'use strict';
angular.module('sf')
  .controller('CaseOverviewCtrl', function($scope, $routeParams, perspectiveService, navigationService) {
    $scope.perspectives = perspectiveService.getPerspectives();
    /*$scope.myCases = perspectiveService.getMyCases();
     $scope.myLatestCases = perspectiveService.getMyLatestCases();
     $scope.myTodaysCases = perspectiveService.getTodaysCases();*/
  });