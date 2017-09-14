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

package io.getlime.push.repository;

import io.getlime.push.repository.model.PushMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface used to access push message database.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Repository
public interface PushMessageRepository extends CrudRepository<PushMessage, Long> {

    /**
     * Find all push messages with given status. Used primarily to obtain pending activations (in PENDING status).
     * @param status Push message status.
     * @return List of all messages with given status.
     */
    List<PushMessage> findByStatus(PushMessage.Status status);

}
