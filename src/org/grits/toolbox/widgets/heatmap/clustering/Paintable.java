/*******************************************************************************
 * Copyright 2013 Lars Behnke
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.grits.toolbox.widgets.heatmap.clustering;

import org.eclipse.draw2d.Graphics;

/** 
 * Implemented by visual components of the dendrogram.
 * @author lars
 * 
 * @modified by Sena Arpinar for swt/draw2d
 *
 */
public interface Paintable {

	void paint(Graphics g, int xDisplayOffset, int yDisplayOffset, double xDisplayFactor, double yDisplayFactor,
			boolean decorated, boolean horizontal);

	void paint(Graphics g, int xDisplayOffset, int yDisplayOffset, double xDisplayFactor, double yDisplayFactor,
			boolean decorated, boolean horizontal, boolean primarySide);
    
}
