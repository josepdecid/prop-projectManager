(function () {
    'use strict';

    /**
     * @ngdoc object
     * @name project.search.author.controller:SearchAuthorCtrl
     *
     * @description
     *
     */
    angular
        .module('project.search')
        .controller('SearchAuthorCtrl', SearchAuthorCtrl);

    function SearchAuthorCtrl($rootScope, $mdDialog) {
        var vm = this;
        vm.ctrlName = 'SearchAuthorCtrl';

        vm.title = 'MENU_SEARCH_AUTHOR';

        vm.selectDocument = function (document) {
            vm.documentSelected = document;
            vm.isAuthorSelected = false;
            vm.isDocumentSelected = true;
        };

        (function showDialog() {
            $mdDialog.show({
                controller: DialogAuthorsController,
                templateUrl: 'search/author/search-author-dialog.tpl.html',
                clickOutsideToClose: false,
                escapeToClose: false
            })}());

        function DialogAuthorsController($rootScope, $scope, $mdDialog) {
            $scope.searchAuthors = function (prefix) {
                try {
                    /*var response = $rootScope.backendService.getAuthorsWithPrefix(prefix);
                    $scope.authors = JSON.parse(response).authors;*/
                    $scope.authors = [
                        {
                            name: 'Josep'
                        }
                    ];
                    $scope.isPrefixSearch = true;
                } catch (e) {
                    if (e.toString().indexOf('AuthorNotFoundException') !== -1) {
                        //TODO
                    }
                }
            };
            $scope.selectAuthor = function (author) {
                try {
                    /*var response = $rootScope.backendService.getDocumentsByAuthorId(author.name);
                    $scope.authors = JSON.parse(response).authors;*/
                    vm.documents = [
                        {
                            title: 'John',
                            author: 'Doe',
                            cover: '',
                            rating: '1',
                            content: 'Bon dia i bona hora'
                        }
                    ];
                    vm.isAuthorSelected = true;
                    $mdDialog.hide();
                } catch (e) {
                    if (e.toString().indexOf('AuthorNotFoundException') !== -1) {
                        //TODO
                    }
                }
            };
        }

    }

}());
