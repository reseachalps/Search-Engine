from lxml import etree
import re
import math


"""
The method used below is described in :
“Language Independent Content Extraction from Web Pages” Arias et al. 2009
(https://www2.cs.kuleuven.be/cwis/research/liir/publication_files/978AriasEtAl2009.pdf)
More info can be found in the wiki :
https://data-publica.atlassian.net/wiki/display/RD/Extraction+du+texte+pertinent
"""


def get_percentile(data, p):
    """
    Given a list of sortable things, returns the Pth percentile of that list
    :param data: list of sortable things
    :param p: Pth percentile to get (from 0 to 100)
    :return: an element from data that’s the Pth percentile
    """
    rank = math.ceil((p/100) * len(data) - 1)
    return sorted(data)[rank]


# Why the ', *, ' ? : arguments following '*' MUST BE specified as keywords.
# so no 'extract(dom, 2, 0.4, 75)'
# For more info : https://www.python.org/dev/peps/pep-3102/
def extract(dom, *, cutoff_radius=4, percentile=95):
    """
    Extract relevant text from an HTML DOM
    :param dom: pyquery DOM
    :param cutoff_radius: radius around selected lines where other lines are be
    selected
    :param percentile: the initial set of selected lines is made of the lines
    above the Nth percentile according to length
    :return: relevant text as a string
    """

    lines = get_lines(dom)

    if len(lines) == 0:
        return ""

    lengths = [len(l[0]) for l in lines]
    is_important = [l[1] for l in lines]

    max_length = get_percentile(lengths, percentile)

    # We select lines that are above the computed threshold
    initial_set = {i for i, l in enumerate(lengths) if l >= max_length or is_important[i]}
    selected_lines = set()
    # For every line we have in the initial set, we search for lines to add
    # in a radius around them
    while len(initial_set) > 0:
        index = initial_set.pop()
        selected_lines.add(index)
        # Computing the radius
        start = max(0, index - cutoff_radius)
        end = min(len(lines), index + cutoff_radius + 1)
        # Adding every line in the radius to the selected set
        selected_lines.update(range(start, end))

    # Sort by original position as to keep order, removing empty lines to have a
    # beautiful text
    selected_lines = [lines[i][0] for i in sorted(selected_lines) if lengths[i] > 0]
    return "\n".join(selected_lines)


text_tags = {
    "a", "b", "big", "em", "font", "i", "q", "s", "small", "strong", "sub", "sup", "u",
    "p", "h4", "h5", "h6", "br", "span"
}

important_tags = {
    "h1", "h2", "h3"
}

ignore_tags = {
    "script", "style", "head"
}


def get_lines(dom):
    """
    Given a DOM object, create a list of lines. A line is text that is
    contained between `block` tags. `block` tags are those that aren’t
    changing the text but are acting as separators (like div, table, etc…)
    :param dom:
    :return:
    """
    lines = []
    current_line = ""
    context = etree.iterwalk(dom.root, events=("start", "end"))
    is_important = False
    for action, node in context:
        # Comments’ tags are not string but functions
        if type(node.tag) != str or node.tag in ignore_tags:
            continue

        # New "block" of content (not a text tag)
        if node.tag not in text_tags:
            lines.append((current_line.strip(), is_important))
            current_line = ""
            # if the parent tag of the block of content is one of important
            # the whole block is important
            is_important = node.tag in important_tags

        if action == "start":
            text = node.text if node.text is not None else ""
        else:
            text = node.tail if node.tail is not None else ""

        if text != "":
            current_line += text + " "

    if current_line != "":
        lines.append((current_line.strip(), is_important))
    return [(re.sub("\s+", " ", l.strip()), imp) for l, imp in lines if len(l) > 0]
