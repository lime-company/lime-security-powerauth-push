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

package io.getlime.push.service.fcm;

/**
 * Class representing the FCM notification payload, used for "FCM Notification Messages".
 *
 * Note that {@link FcmClient} may send data payload alongside the {@link FcmNotification}
 * payload. The data payload is then represented as a generic Map<String, Object>.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class FcmNotification {

    /**
     * Notification title.
     */
    private String title;

    /**
     * Notification text body.
     */
    private String body;

    /**
     * Notification icon name.
     */
    private String icon;

    /**
     * Notification sound name.
     */
    private String sound;

    /**
     * Notification tag.
     */
    private String tag;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
