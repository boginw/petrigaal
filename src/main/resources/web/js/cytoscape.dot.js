// Remix of https://gist.github.com/maccesch/791843a0c6c125f6b04b16106a29e4a0
function CytoscapeDotLayout(options) {
    this.options = options;
}

CytoscapeDotLayout.prototype.run = function () {
    let dotStr = 'digraph G {\n';
    dotStr += "   graph [pad=\"2\", nodesep=\"2\", ranksep=\"1\", rankdir=\"TB\", splines=ortho];\n";

    const nodes = this.options.eles.nodes();
    for (let i = 0; i < nodes.length; ++i) {
        const node = nodes[i];
        dotStr += `  ${node.id()}[label="${node.id()}"];\n`;
    }

    const edges = this.options.eles.edges();
    for (let i = 0; i < edges.length; ++i) {
        const edge = edges[i];
        dotStr += `  ${edge.source().id()} -> ${edge.target().id()};\n`;
    }

    this.options.ranks.forEach(rank => {
        dotStr += "   {rank=same; ";
        rank.forEach(id => {
            dotStr += id + "; ";
        });
        dotStr += "   }\n";
    })

    dotStr += '}';

    const viz = new Viz();

    viz.renderSVGElement(dotStr).then((svg) => {
        console.log(svg);
        const svgNodes = svg.getElementsByClassName('node');
        const idToPositions = {};

        let minY = Number.POSITIVE_INFINITY;

        for (let i = 0; i < svgNodes.length; ++i) {
            const node = svgNodes[i];

            const id = node.getElementsByTagName('title')[0].innerHTML.trim();

            const ellipse = node.getElementsByTagName('ellipse')[0];
            console.log(ellipse);
            const y = ellipse.cy.baseVal.value * 2;

            idToPositions[id] = {x: ellipse.cx.baseVal.value * 2, y};

            minY = Math.min(minY, y);
        }

        nodes.layoutPositions(this, this.options, (ele) => {
            let {x, y} = idToPositions[ele.id()];
            y -= minY - 30;
            return {x, y};
        });
    });

    return this;
};

