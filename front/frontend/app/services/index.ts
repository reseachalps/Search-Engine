import 'angular';

/**
 * App Services
 */

import {SearchService} from './SearchService.ts';
import {StructureService} from './StructureService.ts';
import {FiltersService} from './FiltersService.ts';
import {PublicationService} from './PublicationService.ts';
import {StatsService} from "./StatsService";
import {ProjectService} from "./ProjectService";


angular.module('app.services', [])
    .service('SearchService', SearchService)
    .service('StructureService', StructureService)
    .service('FiltersService', FiltersService)
    .service('StatsService', StatsService)
    .service('ProjectService', ProjectService)
    .service('PublicationService', PublicationService);