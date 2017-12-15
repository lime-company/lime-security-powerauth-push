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
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushMessageSendResult;
import io.getlime.push.model.request.SendPushMessageBatchRequest;
import io.getlime.push.model.request.SendPushMessageRequest;
import io.getlime.push.model.validator.SendPushMessageBatchRequestValidator;
import io.getlime.push.model.validator.SendPushMessageRequestValidator;
import io.getlime.push.service.PushMessageSenderService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller responsible for processes related to push notification sending.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "push/message")
public class PushMessageController {

    private PushMessageSenderService pushMessageSenderService;

    @Autowired
    public PushMessageController(PushMessageSenderService pushMessageSenderService) {
        this.pushMessageSenderService = pushMessageSenderService;
    }

    /**
     * Send a single push message.
     *
     * @param request Send push message request.
     * @return Response with message sending results.
     * @throws PushServerException In case request object is invalid.
     */
    @RequestMapping(value = "send", method = RequestMethod.POST)
    @ApiOperation(value = "Send a single Push message",
                  notes = "Send push message to user, defined in request body - message object by user ID and activation ID," +
                          " using given application ID \n \n" +
                          "Message contains attributes and body\n" +
                          "Attributes describe whether message has to be " +
                          "encrypted, silent (If true, no system UI is displayed), personal (If true and activation is not in ACTIVE state the message is not sent)\n" +
                          "Body consist of body (message), and notification parameters")
    public @ResponseBody ObjectResponse<PushMessageSendResult> sendPushMessage(@RequestBody ObjectRequest<SendPushMessageRequest> request) throws PushServerException {
        SendPushMessageRequest requestObject = request.getRequestObject();
        String errorMessage = SendPushMessageRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final Long appId = requestObject.getAppId();
        final List<PushMessage> pushMessageList = new ArrayList<>();
        pushMessageList.add(requestObject.getMessage());
        PushMessageSendResult result;
        result = pushMessageSenderService.sendPushMessage(appId, pushMessageList);
        return new ObjectResponse<>(result);
    }

    /**
     * Send a batch of push messages.
     *
     * @param request Request with push message batch.
     * @return Response with message sending results.
     * @throws PushServerException In case request object is invalid.
     */
    @RequestMapping(value = "batch/send", method = RequestMethod.POST)
    @ApiOperation(value = "Send batch of push messages",
                  notes = "Send to each user in request body, assigned to application ID, message. Message and user definition is same as in \"send a single push message\" method. " +
                          "Users and their messages are inside request body - batch param.")
    public @ResponseBody ObjectResponse<PushMessageSendResult> sendPushMessageBatch(@RequestBody ObjectRequest<SendPushMessageBatchRequest> request) throws PushServerException {
        SendPushMessageBatchRequest requestObject = request.getRequestObject();
        String errorMessage = SendPushMessageBatchRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final Long appId = requestObject.getAppId();
        final List<PushMessage> batch = requestObject.getBatch();
        PushMessageSendResult result;
        result = pushMessageSenderService.sendPushMessage(appId, batch);
        return new ObjectResponse<>(result);
    }
}