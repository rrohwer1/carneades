/*
Carneades Argumentation Library and Tools.
Copyright (C) 2008 Matthias Grabmair

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License version 3 (GPL-3)
as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for details.
 
You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package Carneades.Graph;

import javafx.ext.swing.*;
import javafx.input.*;
import javafx.animation.*;
import javafx.scene.geometry.*;
import javafx.scene.paint.*;
import Carneades.Graph.*;
import java.lang.System;

// Abstract Controller Class for Interaction
import Carneades.Control.GraphControl;

public class GraphView extends Canvas {
	public attribute focusX: Number = middleX;
	public attribute focusY: Number = middleY;
	public attribute middleX: Number = bind this.width/2;
	public attribute middleY: Number = bind this.height/3;
	
	public attribute graph: Graph;
	override attribute width;
	attribute layout: GraphLayout;
	attribute control: GraphControl;

	attribute backSensor = Rectangle {
		x: 0
		y: 0
		height: bind this.height
		width: bind this.width
		fill: GC.transparent
		visible: true

		onMouseClicked: function(e: MouseEvent): Void {
			if (e.getButton() == 3) {
				control.unSelectAll();
			}
		}

		onMouseDragged: function(e: MouseEvent): Void {
			dragSymbol.x = e.getCanvasX() - 10;
			dragSymbol.y = e.getCanvasY() - 6;
		}
	}

	attribute dragSymbol = Rectangle {
		x: 0
		y: 0
		width: 20
		height: 12
		fill: GC.dragColor
		visible: bind control.dragging
	}

	override attribute background = GC.viewBackground;
	override attribute content = bind [graph, backSensor, dragSymbol ];

	public function reset(): Void {
		graph.root.xShift = middleX;
		graph.root.yShift = middleY - GC.yDistance - GC.vertexDefaultHeight;
	}

	public function focusOn(x: Number, y: Number): Void {
		focusX = x - graph.root.xShift;
		focusY = y - graph.root.yShift;

		var t: Timeline = Timeline {
			repeatCount: 1
			keyFrames: [ 
						KeyFrame {
							time: 0s
							values: [
									 graph.root.xShift => graph.root.xShift tween Interpolator.EASEBOTH,
									 graph.root.yShift => graph.root.yShift tween Interpolator.EASEBOTH
							]
						},
						KeyFrame {
							time: 0.5s
							values: [
									 graph.root.xShift => (middleX - focusX) tween Interpolator.EASEBOTH,
									 graph.root.yShift => (middleY - focusY) tween Interpolator.EASEBOTH
							]
						}
			]
		} // timeline
		t.start();
	}
}
