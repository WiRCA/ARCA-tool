/*
 * Copyright (C) 2011 - 2013 by Eero Laukkanen, Risto Virtanen, Jussi Patana,
 * Juha Viljanen, Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi,
 * Mikko Valjus, Timo Lehtinen, Jaakko Harjuhahto, Jonne Viitanen, Jari Jaanto,
 * Toni Sevenius, Anssi Matti Helin, Jerome Saarinen, Markus Kere
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Implement the relation edge types, which are a line and an arrow with balls in middle of them
function implementEdgeTypes() {
    $jit.ForceDirected.Plot.EdgeTypes.implement({
        'relationArrow': {
            'render': function(adj, canvas) {
                var from = adj.nodeFrom.pos.clone().getc(true),
                    to = adj.nodeTo.pos.clone().getc(true),
                    dim = adj.getData('dim'),
                    direction = adj.data.$direction,
                    inv = !(direction && direction.length>1 && direction[0] != adj.nodeFrom.id);

                if (inv) {
                    from = this.edgeHelper.arrow.calculateArrowPosition(from, to, adj, inv);
                }
                else {
                    to = this.edgeHelper.arrow.calculateArrowPosition(from, to, adj, inv);
                }

                var ctx = canvas.getCtx();
                // invert edge direction
                if (inv) {
                    var tmp = from;
                    from = to;
                    to = tmp;
                }
                var vect = new $jit.Complex(to.x - from.x, to.y - from.y);
                vect.$scale(dim / vect.norm());
                var intermediatePoint = new $jit.Complex(to.x - vect.x, to.y - vect.y),
                    normal = new $jit.Complex(-vect.y / 2, vect.x / 2),
                    v1 = intermediatePoint.add(normal),
                    v2 = intermediatePoint.$add(normal.$scale(-1)),
                    mid = new $jit.Complex(from.x + (to.x - from.x) / 1.8, from.y + (to.y - from.y) / 1.8);

                ctx.beginPath();
                ctx.moveTo(from.x, from.y);
                ctx.lineTo(to.x, to.y);
                ctx.stroke();
                ctx.beginPath();
                ctx.moveTo(v1.x, v1.y);
                ctx.lineTo(v2.x, v2.y);
                ctx.lineTo(to.x, to.y);
                ctx.closePath();

                ctx.fill();
                ctx.beginPath();
                ctx.arc(mid.x, mid.y, 6, 0, 2 * Math.PI, false);
                ctx.closePath();
                ctx.fill();
            },
            'contains': function(adj, pos) {
                var fromNode = adj.nodeFrom.pos.getc(true),
                    toNode = adj.nodeTo.pos.getc(true),
                    epsilon = this.edge.epsilon;

                var fromNode2 = toNode;
                
                toNode = this.edgeHelper.arrow.calculateArrowPosition(fromNode, toNode, adj, false);
                var midPoint = new $jit.Complex(fromNode.x + (toNode.x - fromNode.x) / 1.8, fromNode.y + (toNode.y - fromNode.y) / 1.8);
                var dist = Math.sqrt(Math.pow(pos.x - midPoint.x, 2) + Math.pow(pos.y - midPoint.y, 2));
                var isInside = Math.abs(dist - 6) <= epsilon;

                var toNode2 = this.edgeHelper.arrow.calculateArrowPosition(fromNode, toNode, adj, true);
                var midPoint2 = new $jit.Complex(fromNode2.x + (toNode2.x - fromNode2.x) / 1.8, fromNode2.y + (toNode2.y - fromNode2.y) / 1.8);
                var dist2 = Math.sqrt(Math.pow(pos.x - midPoint2.x, 2) + Math.pow(pos.y - midPoint2.y, 2));
                var isInside2 = Math.abs(dist2 - 6) <= epsilon;
                return (isInside || isInside2);
            }
        },

        'relationLine': {
            'render': function(adj, canvas) {
                var from = adj.nodeFrom.pos.clone().getc(true),
                    to = adj.nodeTo.pos.clone().getc(true),
                    mid = new $jit.Complex(from.x + (to.x - from.x) / 2, from.y + (to.y - from.y) / 2),
                    dim = adj.getData('dim');
                var ctx = canvas.getCtx();
                setGlow(ctx, "black", 0, 0, adj.getData('glow'));
                ctx.beginPath();
                ctx.moveTo(from.x, from.y);
                ctx.lineTo(to.x, to.y);
                ctx.stroke();
                ctx.beginPath();
                ctx.arc(mid.x, mid.y, 6, 0, 2 * Math.PI, true);
                ctx.closePath();
                ctx.fill();
                setGlow(ctx, "black", 0, 0, 0);
            },

            'contains': function(adj, pos) {

                var from = adj.nodeFrom.pos.getc(true),
                    to = adj.nodeTo.pos.getc(true),
                    epsilon = this.edge.epsilon,
                    mid = new $jit.Complex(from.x + (to.x - from.x) / 2, from.y + (to.y - from.y) / 2);

                var dist = Math.sqrt(Math.pow(pos.x - mid.x, 2) + Math.pow(pos.y - mid.y, 2));
                return Math.abs(dist - 6) <= epsilon;
            }
        },
        'circleline': {
            'render': function(adj, canvas) {
                var from = adj.nodeFrom.pos.clone().getc(true);
                var to = adj.nodeTo.pos.clone().getc(true);
                var ctx = canvas.getCtx();

                var dx = Math.abs(from.x - to.x);
                if (dx == 0) {
                    dx = 100;
                }
                ctx.beginPath();
                ctx.arc(from.x, from.y-12, (dx/4), 0, 3, true);
                ctx.stroke();
                ctx.closePath();

                var mid = {
                    x: from.x,
                    y: from.y - (dx/2.7)
                }
                ctx.beginPath();
                ctx.arc(mid.x, mid.y, 6, 0, 2 * Math.PI, true);
                ctx.closePath();
                ctx.fill();

            },
            contains : function (adj, pos) {
                var from = adj.nodeFrom.pos.getc(true);
                var to = adj.nodeTo.pos.getc(true);
                var dx = Math.abs(from.x - to.x);
                if (dx == 0) {
                    dx = 100;
                }
                var mid = {
                    x: from.x,
                    y: from.y - (dx/2.7)
                }
                var epsilon = this.edge.epsilon;

                var dist = Math.sqrt(Math.pow(pos.x - mid.x, 2) + Math.pow(pos.y - mid.y, 2));
                return Math.abs(dist - 6) <= epsilon;
            }
        }
    });
}


/**
 * Set shadow to create a glow effect. Please notice that the context element is left with the glow effect.
 * Therefore setGlow should be called again with blur value 0 when glow isn't needed anymore.
 * @param ctx Canvas context
 * @param color Glow color
 * @param ox Offset in x axis
 * @param oy Offset in y axis
 * @param blur Blur level
 */
function setGlow(ctx, color, ox, oy, blur) {
    ctx.shadowColor = color;
    ctx.shadowOffsetX = ox;
    ctx.shadowOffsetY = oy;
    ctx.shadowBlur = blur;
}