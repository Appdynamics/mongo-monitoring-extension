/**
 * Copyright 2013 AppDynamics
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

package com.appdynamics.monitors.mongo.json;

public class Opcounters {
	private Number insert;
	private Number query;
	private Number update;
	private Number delete;
	private Number getmore;
	private Number command;

	public Number getInsert() {
		return insert;
	}
	public void setInsert(Number insert) {
		this.insert = insert;
	}
	public Number getQuery() {
		return query;
	}
	public void setQuery(Number query) {
		this.query = query;
	}
	public Number getUpdate() {
		return update;
	}
	public void setUpdate(Number update) {
		this.update = update;
	}
	public Number getDelete() {
		return delete;
	}
	public void setDelete(Number delete) {
		this.delete = delete;
	}
	public Number getGetmore() {
		return getmore;
	}
	public void setGetmore(Number getmore) {
		this.getmore = getmore;
	}
	public Number getCommand() {
		return command;
	}
	public void setCommand(Number command) {
		this.command = command;
	}
	
}
