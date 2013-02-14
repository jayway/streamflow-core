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

  window.sf = {
    directives: angular.module('sf.directives', []),
    filters: angular.module('sf.filters', []),
    controllers: angular.module('sf.controllers', []),
  };

  sf.env = sf.env || 'development';

  angular.module('sf', ['sf.filters', 'sf.controllers', 'sf.directives'])
    .config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
      // $locationProvider.html5Mode(true);
      $routeProvider
      .when('/:projectId/:caseType', {
        templateUrl: 'modules/views/case-list.html',
        controller: 'CaseListCtrl'
      })
      .when('/:projectId/:caseType/:caseId', {
        templateUrl:'modules/views/case-detail.html',
        controller: 'CaseDetailCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
  }]);


})();
