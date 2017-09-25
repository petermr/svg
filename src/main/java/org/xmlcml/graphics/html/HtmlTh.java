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


/** base class for lightweight generic SVG element.
 * no checking - i.e. can take any name or attributes
 * @author pm286
 *
 */
public class HtmlTh extends HtmlElement {
	@SuppressWarnings("unused")
	private final static Logger LOG = Logger.getLogger(HtmlTh.class);
	public final static String TAG = "th";

	/** constructor.
	 * 
	 */
	public HtmlTh() {
		super(TAG);
	}
	/**
	 * create a Td with the included text
	 * @param content
	 * @return
	 */
	public static HtmlTh createAndWrapText(String content) {
		HtmlTh th = new HtmlTh();
		th.appendChild(content);
		return th;
	}


}