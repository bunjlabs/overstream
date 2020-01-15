/*
 * Copyright 2019 Bunjlabs
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

package com.overstreamapp.streamlabs.events;

import com.overstreamapp.keeper.EventObject;

public class TwitchSub implements EventObject {
    public String name;
    public int months;
    public String message;
    public String subPlan;
    public String subPlanName;
    public String subType;

    public TwitchSub() {
    }

    public TwitchSub(String name, int months, String message, String subPlan, String subPlanName, String subType) {
        this.name = name;
        this.months = months;
        this.message = message;
        this.subPlan = subPlan;
        this.subPlanName = subPlanName;
        this.subType = subType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubPlan() {
        return subPlan;
    }

    public void setSubPlan(String subPlan) {
        this.subPlan = subPlan;
    }

    public String getSubPlanName() {
        return subPlanName;
    }

    public void setSubPlanName(String subPlanName) {
        this.subPlanName = subPlanName;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }
}
