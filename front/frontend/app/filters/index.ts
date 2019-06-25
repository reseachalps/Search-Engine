import {capitalize} from "./capitalize";
import {localeCompareString} from "./localeCompareString";
import {highlightLabel} from "./highlightLabel";
import {filterCountNoHeaders} from "./filterCountNoHeaders";
import {joinFilter} from "./joinFilter";

angular.module('app.filters', [])
    .filter('localeCompareString', localeCompareString)
    .filter('filterCountNoHeaders', filterCountNoHeaders)
    .filter('highlightLabel', highlightLabel)
    .filter('join', joinFilter)
    .filter('capitalize', capitalize);