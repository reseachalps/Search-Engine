/**
 * returns the label for a highlight category
 * @returns a string
 */

export function highlightLabel() {
    return (input) => {
        return CAT_DIC[input] || input;
    }
}

const CAT_DIC = {
  'websiteContents': 'Web site content',
  'projects': 'Project',
  'activityLabels': 'Fields',
  'acronym': 'Acronyms',
  'label': 'Label',
  'id': 'Identifier',
  'publications': 'Publication',
  'publications.authors': 'Publications author',
};