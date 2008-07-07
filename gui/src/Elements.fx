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


package GraphSketch1.Graph.Elements;

import javafx.scene.geometry.*;
import javafx.scene.paint.*;
import javafx.scene.transform.*;
import javafx.scene.text.*;
import javafx.scene.*;
import javafx.input.*;

import java.lang.System;
import java.lang.Math;

// import the necessary parts of the model
import GraphSketch1.Argument.Argument;
import GraphSketch1.Argument.Argument.*;

// Other View Imports
import GraphSketch1.Graph.*;

// Abstract Controller Import
import GraphSketch1.Control.AbstractGraphControl;

public abstract class ArgumentElement extends Vertex {
	attribute fill: Color = Color.WHITE;
	attribute mainRect: Rectangle = Rectangle {
					x: bind x - (width / 2).intValue()
					y: bind y
					width: bind width
					height: bind height
					fill: bind fill
					stroke: Color.BLACK
					strokeWidth: 1
				
					onMouseClicked: function(e: MouseEvent) {
						control.unSelectAll();
						selected = true;
						//GC.p("Clicked: " + caption + " x/y: " + x + " / " + y + " P: " + parentVertex.caption+ " L: " + level + " #C: " + sizeof children);
						//GC.p("Width: " + width + " Height: " + height + " Index: " + index );
						control.processSelection();
					}

					onMouseDragged: function(e: MouseEvent) {
						if (this.selected) { control.startDrag(); }
					}	

					onMouseReleased: function(e: MouseEvent) {
						if (control.dragging) {	control.endDrag(); }
					}

					onMouseEntered: function(e: MouseEvent) {
						if (control.dragging) { control.setDraggingOver(this); }
					}

					onMouseExited: function(e: MouseEvent) {
						if (control.dragging) { control.setDraggingOver(null); }
					}

				} // main rect

	override attribute text = Text {
					content: bind caption
					verticalAlignment: VerticalAlignment.TOP
					horizontalAlignment: HorizontalAlignment.CENTER
					x: bind x
					y: bind y + (height / 2)
				} // Text

	attribute selection: Rectangle = Rectangle {
					x: bind x - (width / 2) - 5
					y: bind y - 5
					width: bind width + 10
					height: bind height + 10
					stroke: bind {if (control.dragging) GC.dragColor else GC.selectionColor};
					strokeWidth: 2
					visible: bind selected
				} // selection rect

}

public class ArgumentBox extends ArgumentElement {
	public attribute argument: Argument;
	override attribute caption = bind argument.id;
	override attribute fill = bind {if (argument.ok) Color.LIGHTGREY else Color.WHITE};

	public function create():Node {
		mainRect.arcWidth = height;
		mainRect.arcHeight = height;

		Group {
			content: [
				mainRect
				, selection
				, text
			] // content
		} // Group
	} // composeNode
}

public class StatementBox extends ArgumentElement {
	public attribute statement: Statement;
	override attribute caption = bind statement.id;
	override attribute fill = bind {if ( statement.ok 
										or statement.value == "true") Color.LIGHTGREY else Color.WHITE};
	
	public function create():Node {
		Group {
			content: [
				mainRect
				, selection
				, text
			] // content
		} // Group
	} // composeNode
}

public class Arrow extends Edge {
	attribute headSize: Number = 10;
	attribute heading: Number = 0; // 0 means it points right, 1 it points left, straight up or down does not matter
	attribute fill: Color = Color.BLACK;
	override attribute stroke = Color.BLACK;
	override attribute turnHead = true;

	postinit {
		setAngle();
	}

	public function create():Node {
		Group {
			content: [
				line,
				Polygon {
					transform: bind [Transform.rotate(angle, x2, y2)]

					points: bind [ x2, y2, 
								x2 - headSize, y2+(headSize / 2),
								x2 - headSize, y2-(headSize / 2)]
					stroke: bind stroke
					strokeWidth: bind strokeWidth
					fill: bind fill
				}
			] // content
		} // group
	}
}

public class ArgumentLink extends Arrow {
	override attribute headSize = 10;
}

public class ConArgumentLink extends ArgumentLink {
	//override attribute xHeadShift = bind headSize / 2;
	override attribute yHeadShift = bind headSize / 3;
	override attribute fill = Color.WHITE
}

public class ProArgumentLink extends ArgumentLink {
	//override attribute xHeadShift = bind headSize / 2;
	override attribute yHeadShift = bind headSize / 3;
	override attribute fill = Color.BLACK
}

public class PremiseLink extends Edge {
	public attribute premise: Premise;
	attribute radius: Number = 5;
	attribute negWidth: Number = 10;
	attribute negStrokeWidth: Number = 2;

	public attribute negated: Boolean = false;
	override attribute turnHead = bind negated;
	
	override attribute yHeadShift = bind radius;

	attribute negation: Line = Line {
					startX: bind x2 - Math.max(yHeadShift * 2, negWidth)
					startY: bind y2 - (negWidth / 2) 
					endX: bind x2 - Math.max(yHeadShift * 2, negWidth)
					endY: bind y2 + (negWidth / 2)
					transform: bind [Transform.rotate(angle, x2, y2)]
					stroke: bind stroke
					strokeWidth: bind negStrokeWidth
					visible : bind negated
				}

	attribute exceptionHead: Circle = Circle {
   				     centerX: bind x2
   				     centerY: bind y2
   	    			 radius: bind radius
   				     fill: Color.WHITE
   				     stroke: Color.BLACK
					 visible: bind premise.exception
    			}

	public function negate() {
		if (negated) {
			negated = false;
		} else {
			negated = true;
		}
	}

	public function create():Node {
		Group {
			content: [
				line, negation, exceptionHead
			] // content
		} // Group
	} // composeNode
}

