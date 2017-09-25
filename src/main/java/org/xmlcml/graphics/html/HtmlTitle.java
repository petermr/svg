/**
 *    Copyright 2011 Peter Murray-Rust et. al.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.xmlcml.graphics.html;

import org.apache.log4j.Logger;

/** 
 <thead>
    <tr>
      <th>Month</th>
      <th>Savings</th>
    </tr>
  </thead>

 * @author pm286
 *
 */
public class HtmlTitle extends HtmlElement {
	@SuppressWarnings("unused")
	private final static Logger LOG = Logger.getLogger(HtmlTitle.class);
	public final static String TAG = "title";

	/** constructor.
	 * 
	 */
	public HtmlTitle() {
		super(TAG);
	}
	
	/** constructor.
	 *
	 */
	public HtmlTitle(String content) {
		this();
		this.appendChild(content);
	}
	
}
