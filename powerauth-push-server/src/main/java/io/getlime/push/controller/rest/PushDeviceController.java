/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.push.controller.rest;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.powerauth.soap.ActivationStatus;
import io.getlime.powerauth.soap.GetActivationStatusResponse;
import io.getlime.powerauth.soap.GetPersonalizedEncryptionKeyResponse;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.request.CreateDeviceRequest;
import io.getlime.push.model.request.DeleteDeviceRequest;
import io.getlime.push.model.request.UpdateDeviceStatusRequest;
import io.getlime.push.model.validator.CreateDeviceRequestValidator;
import io.getlime.push.model.validator.DeleteDeviceRequestValidator;
import io.getlime.push.model.validator.UpdateDeviceStatusRequestValidator;
import io.getlime.push.repository.PushDeviceRepository;
import io.getlime.push.repository.model.PushDeviceRegistrationEntity;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

/**
 * Controller responsible for device registration related business processes.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "push/device")
public class PushDeviceController {

    private PushDeviceRepository pushDeviceRepository;
    private PowerAuthServiceClient client;

    @Autowired
    public PushDeviceController(PushDeviceRepository pushDeviceRepository) {
        this.pushDeviceRepository = pushDeviceRepository;
    }

    @Autowired
    void setClient(PowerAuthServiceClient client) {
        this.client = client;
    }

    /**
     * Create a new device registration.
     * @param request Device registration request.
     * @return Device registration status.
     * @throws PushServerException In case request object is invalid.
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    @ApiOperation(value = "Create a device",
                  notes = "Create a new device push token (platform specific). Optionally, the call may include also activationId, so that the token is associated with given user." +
                          "Request body should contain application ID, device token, device's platform and optionally an activation ID. " +
                          "If such device already exist, date on last registration is updated and also platform might be changed\n" +
                          "\n---" +
                          "Note: Since this endpoint is usually called by the back-end service, it is not secured in any way. " +
                          "It's the service that calls this endpoint responsibility to assure that the device is somehow authenticated before the push token is assigned with given activationId value," +
                          " so that there are no incorrect bindings.")
    public @ResponseBody Response createDevice(@RequestBody ObjectRequest<CreateDeviceRequest> request) throws PushServerException {
        CreateDeviceRequest requestedObject = request.getRequestObject();
        String errorMessage = CreateDeviceRequestValidator.validate(requestedObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        Long appId = requestedObject.getAppId();
        String pushToken = requestedObject.getToken();
        String platform = requestedObject.getPlatform();
        String activationId = requestedObject.getActivationId();
        PushDeviceRegistrationEntity device = pushDeviceRepository.findFirstByAppIdAndPushToken(appId, pushToken);
        if (device == null) {
            device = new PushDeviceRegistrationEntity();
            device.setAppId(appId);
            device.setPushToken(pushToken);
        }
        device.setTimestampLastRegistered(new Date());
        device.setPlatform(platform);
        if (activationId != null) {
            final GetActivationStatusResponse activation = client.getActivationStatus(activationId);
            if (activation != null && !ActivationStatus.REMOVED.equals(activation.getActivationStatus())) {
                device.setActivationId(activationId);
                device.setActive(activation.getActivationStatus().equals(ActivationStatus.ACTIVE));
                device.setUserId(activation.getUserId());
                if (activation.getActivationStatus().equals(ActivationStatus.ACTIVE)) {
                    final GetPersonalizedEncryptionKeyResponse encryptionKeyResponse = client.generatePersonalizedE2EEncryptionKey(activationId, null);
                    if (encryptionKeyResponse != null) {
                        device.setEncryptionKey(encryptionKeyResponse.getEncryptionKey());
                        device.setEncryptionKeyIndex(encryptionKeyResponse.getEncryptionKeyIndex());
                    }
                }
            }
        }
        pushDeviceRepository.save(device);
        return new Response();
    }

    /**
     * Update activation status for given device registration.
     * @param request Status update request.
     * @return Status update response.
     * @throws PushServerException In case request object is invalid.
     */
    @RequestMapping(value = "status/update", method = RequestMethod.POST)
    @ApiOperation(value = "Update device status",
                  notes = "Update the status of given device registration based on the associated activation ID. " +
                          "This can help assure that registration is in non-active state and cannot receive personal messages.")
    public @ResponseBody Response updateDeviceStatus(@RequestBody UpdateDeviceStatusRequest request) throws PushServerException {
        String errorMessage = UpdateDeviceStatusRequestValidator.validate(request);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        String activationId = request.getActivationId();
        List<PushDeviceRegistrationEntity> device = pushDeviceRepository.findByActivationId(activationId);
        if (device != null)  {
            ActivationStatus status = client.getActivationStatus(activationId).getActivationStatus();
            for (PushDeviceRegistrationEntity registration: device) {
                registration.setActive(status.equals(ActivationStatus.ACTIVE));
                pushDeviceRepository.save(registration);
            }
        }
        return new Response();
    }

    /**
     * Remove device registration with given push token.
     * @param request Remove registration request.
     * @return Removal status response.
     * @throws PushServerException In case request object is invalid.
     */
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @ApiOperation(value = "Delete a device",
                  notes = "Remove device identified by application ID and device token. " +
                          "If device identifiers don't match, nothing happens")
    public @ResponseBody Response deleteDevice(@RequestBody ObjectRequest<DeleteDeviceRequest> request) throws PushServerException {
        DeleteDeviceRequest requestedObject = request.getRequestObject();
        String errorMessage = DeleteDeviceRequestValidator.validate(requestedObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        Long appId = requestedObject.getAppId();
        String pushToken = requestedObject.getToken();
        PushDeviceRegistrationEntity device = pushDeviceRepository.findFirstByAppIdAndPushToken(appId, pushToken);
        if (device != null)  {
            pushDeviceRepository.delete(device);
        }
        return new Response();
    }
}