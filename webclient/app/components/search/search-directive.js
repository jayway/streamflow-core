'use strict';

angular.module('sf').directive('search', function ($location, navigationService) {
  return {
    restrict: 'E',
    templateUrl: 'components/search/search.html',
    link: function (scope, element) {
      // Translation map. Should use i18n for this.
      scope.searchTerms = {
        'createdOn': 'skapad',
        'caseType': 'ärendetyp',
        'project': 'projekt',
        'label': 'etikett',
        'assignedTo': 'tilldelad',
        'name': 'namn',
        'contactId': 'kontakdid',
        'phoneNumber': 'telefon',
        'emailAddress': 'email',
        'today': 'idag',
        'yesterday': 'igår',
        'hour': 'timme',
        'week': 'vecka',
        'me': 'mig',
        'createdBy': 'skapadav'
      };

      scope.addSearchTermToQuery = function (event) {
        // Get key from search term value.
        var searchTerm = _.invert(scope.searchTerms)[event.currentTarget.innerHTML] + ':';

        // Don't let query be undefined.
        scope.query = scope.query || '';

        // Add a whitespace to seperate from previous search value.
        scope.query += (scope.query.length === 0) ? searchTerm : ' ' + searchTerm;

        // Defer focus of text field to next cycle to avoid problems with
        // $apply. There must be a better way to do this?
        _.defer(function () {
          element.find('#main-searchtext').focus();
        });
      };

      scope.search = function (query) {
        $location.search({'query': query});
        navigationService.linkTo('/search');
      };
    }
  };
});
