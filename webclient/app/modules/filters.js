/*
 *
 * Copyright 2009-2012 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function() {
  'use strict';

  sf.filters.filter('positive', function() {
    return function(input) {
      return input > 0 ? input : '';
    };
  });

  sf.filters.filter('shortDate', ['$filter', function($filter) {
    return function(input) {
      return $filter('date')(input, 'MM/dd');
    };
  }]);

  sf.filters.filter('longDate', ['$filter', function($filter) {
    return function(input) {
      return $filter('date')(input, 'yyyy-MM-dd');
    };
  }]);

  sf.filters.filter('translate', ['$filter', function($filter) {
    return function(input) {

      // So far, we keep it simple by just using a lookup table
      var translation = {
        inbox: 'Inkorg',
        assignments: "Ärenden"
      };

      return translation[input];
    };
  }]);

}());

