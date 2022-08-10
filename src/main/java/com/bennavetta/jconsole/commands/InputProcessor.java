/**
 * Copyright (C) 2012 Ben Navetta <ben.navetta@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------
 * Modified by: woodrow73 https://github.com/woodrow73
 */
package com.bennavetta.jconsole.commands;

import com.bennavetta.jconsole.tui.console.Console;

public interface InputProcessor {

	/**
	 * Processes the user's input.
	 * @param console The console that the user is interacting with.
	 * @param raw The user's inputted text.
	 * @param args The user's inputted text split into arguments.
	 */
	public void process(Console console, String raw, String... args);

	/** A no operation input processor. */
	public static final InputProcessor NO_OP = (console, raw, args) -> { /* no-op */ };
}
