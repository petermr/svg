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
public class HtmlFrameset extends HtmlElement {
	private final static Logger LOG = Logger.getLogger(HtmlFrameset.class);
	public final static String TAG = "frameset";

	/** constructor.
	 * 
	 */
	public HtmlFrameset() {
		super(TAG);
	}
	
	public void setCols(String cols) {
		this.setAttribute("cols", cols);
	}
	
	public void setRows(String rows) {
		this.setAttribute("rows", rows);
	}
}