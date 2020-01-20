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

package com.overstreamapp.ympd.state;

import java.util.Objects;

public class PlayerState {
    private int state;
    private int totalTime;
    private int elapsedTime;

    public PlayerState() {
    }

    public PlayerState(int state, int totalTime, int elapsedTime) {
        this.state = state;
        this.totalTime = totalTime;
        this.elapsedTime = elapsedTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerState that = (PlayerState) o;
        return state == that.state &&
                totalTime == that.totalTime &&
                elapsedTime == that.elapsedTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, totalTime, elapsedTime);
    }
}
