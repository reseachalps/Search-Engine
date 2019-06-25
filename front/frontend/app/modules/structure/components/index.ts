import './identity'
import './relations-graph'
import './projects'
import './thesis'
import './patents'
import './financial'
import './financial-private'
import './publications'
import './spinoffs'
import './sources'
import {EditingDirective} from "./editing/editing";
import {TagCloudDirective} from "./tag-cloud/tagCloud";

angular.module('app.structure.components', [
        'app.structure.components.identity',
        'app.structure.components.graph',
        'app.structure.components.publications',
        'app.structure.components.thesis',
        'app.structure.components.financial',
        'app.structure.components.financialPrivate',
        'app.structure.components.patents',
        'app.structure.components.spinoffs',
        'app.structure.components.projects',
        'app.structure.components.sources'
    ])
    .directive('editing', <any> EditingDirective)
    .directive('tagCloud', <any> TagCloudDirective);
