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
 */
package com.bennavetta.jconsole.completion;

import com.bennavetta.jconsole.completion.CachingCompletionSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultCompletionSource extends CachingCompletionSource {
	private List<String> terms;
	
	public DefaultCompletionSource(String... terms)
	{
		this(Arrays.asList(terms));
	}
	
	public DefaultCompletionSource(List<String> terms)
	{
		this.terms = terms;
	}
	
	@Override
	protected List<String> doCompletion(String input) {
		List<String> matches = new ArrayList<String>();
		for(String term : terms) {
			if(term.toLowerCase().startsWith(input.toLowerCase())) {
				matches.add(term);
			}
		}
		return matches;
	}

}
