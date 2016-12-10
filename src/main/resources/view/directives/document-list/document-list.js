(function () {
    'use strict';

    /**
     * @ngdoc component
     * @name project.directives.directive:DocumentList
     *
     * @description
     *
     */
    angular
        .module('project')
        .controller('DocumentListCtrl', DocumentListCtrl)
        .directive('documentList', documentList);

    function DocumentListCtrl() {
        var vm = this;
        vm.ctrlName = 'DocumentListCtrl';
        vm.title = "TITLE_DOCUMENT_LIST";
    }

    function documentList($rootScope, $mdDialog, $mdToast, $filter, $timeout, $state) {
        return {
            restrict: 'EA',
            templateUrl: 'directives/document-list/document-list.tpl.html',
            scope: {
                documents: '=ngModel',
                isEditMode: '=?',
                isSearchMode : '=?',
                parentTitle: '=?',
                parent: '=?'
            },
            link: function (scope) {
                scope.isDocFavourite = function(doc){
                    return $rootScope.backendService.isDocumentFavourite(doc.title,doc.author);
                };

                scope.importDocument = function(){
                    try {
                        var response = $rootScope.backendService.importDocument();
                        addDocumentOrdered(scope.documents, JSON.parse(response));
                        showToast('TOAST_DOCUMENT_IMPORTED_SUCCESSFULLY');
                    } catch (e) {
                        showToast(treatException(e), true);
                    }
                };

                scope.removeFavourite = function (doc) {
                    $rootScope.backendService.removeFavourite(doc.title, doc.author);
                    var index = scope.documents.indexOf(doc);
                    scope.documents.splice(index, 1);
                };

                scope.isListSelected = true;
                scope.isButtonOpened = false;
                scope.tooltipVisible = false;
                scope.isDocumentSelected = false;
                scope.isCreateOrUpdate = false;

                if (angular.isUndefined(scope.isEditMode)) {
                    scope.isEditMode = false;
                }

                scope.$watch('scope.isButtonOpened', function(isOpen) {
                    if (isOpen) {
                        $timeout(function() {
                            scope.tooltipVisible = scope.isButtonOpened;
                        }, 600);
                    } else {
                        scope.tooltipVisible = scope.isButtonOpened;
                    }
                });

                scope.createDocument = function () {
                    scope.title = 'MENU_MANAGEMENT_CREATE';
                    scope.documentSelected = buildDocument();
                    scope.isListSelected = false;
                    scope.isNewDocument = true;
                    scope.isCreateOrUpdate = true;
                };

                scope.editDocument = function (document) {
                    scope.title = 'MENU_MANAGEMENT_UPDATE';
                    scope.documentBackUp = angular.copy(document);
                    scope.documentSelected = document;
                    scope.isListSelected = false;
                    scope.isNewDocument = false;
                    scope.isCreateOrUpdate = true;
                };

                scope.backToList = function () {
                    var translations = {
                        title: $filter('translate')('DIALOG_BACK_TITLE'),
                        textContent: $filter('translate')('DIALOG_BACK_CONTENT'),
                        ariaLabel: $filter('translate')('DIALOG_BACK_ARIA_LABEL'),
                        ok: $filter('translate')('DIALOG_BACK_OK'),
                        cancel: $filter('translate')('DIALOG_BACK_CANCEL')
                    };

                    var confirm = $mdDialog.confirm()
                        .title(translations.title)
                        .textContent(translations.textContent)
                        .ariaLabel(translations.ariaLabel)
                        .targetEvent(event)
                        .ok(translations.ok)
                        .cancel(translations.cancel);

                    $mdDialog.show(confirm).then(function () {
                        scope.documentBackUp = undefined;
                        scope.isCreateOrUpdate = false;
                        scope.isListSelected = true;
                        $mdDialog.hide();
                    }, function () {
                    });
                };

                scope.selectDocument = function (document) {
                    scope.documentSelected = document;
                    scope.isListSelected = false;
                    scope.isDocumentSelected = true;
                };

                scope.back = function () {
                    scope.isDocumentSelected = false;
                    scope.isListSelected = true;
                };

                scope.searchAgain = function () {
                    $state.reload();
                };

                scope.selectImage = function () {
                    var path = $rootScope.backendService.selectImage();
                    scope.selectedImage = path;
                };


                scope.storeDocument = function (event) {
                    var translations = {
                        title: scope.isNewDocument ? $filter('translate')('DIALOG_CREATE_TITLE') : $filter('translate')('DIALOG_EDIT_TITLE'),
                        textContent: scope.isNewDocument ? $filter('translate')('DIALOG_CREATE_CONTENT') : $filter('translate')('DIALOG_EDIT_CONTENT'),
                        ariaLabel: scope.isNewDocument ? $filter('translate')('DIALOG_CREATE_ARIA_LABEL') : $filter('translate')('DIALOG_EDIT_ARIA_LABEL'),
                        ok: scope.isNewDocument ? $filter('translate')('DIALOG_CREATE_OK') : $filter('translate')('DIALOG_EDIT_OK'),
                        cancel: scope.isNewDocument ? $filter('translate')('DIALOG_CREATE_CANCEL') : $filter('translate')('DIALOG_EDIT_CANCEL')
                    };

                    var confirm = $mdDialog.confirm()
                        .title(translations.title)
                        .textContent(translations.textContent)
                        .ariaLabel(translations.ariaLabel)
                        .targetEvent(event)
                        .ok(translations.ok)
                        .cancel(translations.cancel);

                    $mdDialog.show(confirm).then(function() {
                        scope.documentSelected.title = scope.documentSelected.title.capitalizeFirstLetter();
                        scope.documentSelected.author = scope.documentSelected.author.capitalizeFirstLetter();
                        if(angular.isDefined(scope.selectedImage)){
                            alert("IMAGEN SELECCIONADA: " + scope.selectedImage);
                            scope.documentSelected.cover = scope.selectedImage;
                        }

                        if (scope.isNewDocument) {
                            var data = JSON.stringify(scope.documentSelected);
                            try {
                                var response = $rootScope.backendService.storeNewDocument(data);
                                var doc = JSON.parse(response);
                                addDocumentOrdered(scope.documents, doc);
                                showToast('TOAST_CREATED_DOCUMENT');
                            } catch (e) {
                                scope.isInvalidData = treatException(e);
                            }
                        } else {
                            var newData = JSON.stringify(scope.documentSelected);
                            var oldData = JSON.stringify(scope.documentBackUp);
                            try {
                                $rootScope.backendService.updateDocument(oldData, newData);
                                var index = scope.documents.indexOf(scope.documentBackUp);
                                scope.documents.splice(index, 1);
                                addDocumentOrdered(scope.documents, scope.documentSelected);
                                showToast('TOAST_EDITED_DOCUMENT');
                            } catch (e) {
                                scope.isInvalidData = treatException(e);
                            }
                        }
                        scope.isCreateOrUpdate = false;
                        scope.isListSelected = true;
                        scope.selectedImage = undefined;
                    }, function() {
                    });

                };

                scope.showDeleteConfirm = function (event, document) {
                    var translations = {
                        title: $filter('translate')('DIALOG_DELETE_TITLE'),
                        textContent: $filter('translate')('DIALOG_DELETE_CONTENT'),
                        ariaLabel: $filter('translate')('DIALOG_DELETE_ARIA_LABEL'),
                        ok: $filter('translate')('DIALOG_DELETE_OK'),
                        cancel: $filter('translate')('DIALOG_DELETE_CANCEL')
                    };

                    var confirm = $mdDialog.confirm()
                        .title(translations.title)
                        .textContent(translations.textContent)
                        .ariaLabel(translations.ariaLabel)
                        .targetEvent(event)
                        .ok(translations.ok)
                        .cancel(translations.cancel);

                    $mdDialog.show(confirm).then(function() {
                        $rootScope.backendService.deleteDocument(document.title, document.author);
                        var index = scope.documents.indexOf(document);
                        scope.documents.splice(index, 1);
                        showToast('TOAST_DELETED_DOCUMENT');
                    }, function() {
                    });
                };

                //////////

                function buildDocument() {
                    return {
                        title: '',
                        author: '',
                        cover: '',
                        content: '',
                        relevance: '',
                        rating: 0
                    }
                }

                function getIndexToInsert(documentsList, newDocument) {
                    var minIndex = 0;
                    var maxIndex = documentsList.length - 1;
                    var currentIndex;

                    while (minIndex <= maxIndex) {
                        currentIndex = (minIndex + maxIndex) / 2 | 0;
                        if (documentsList[currentIndex].title < newDocument.title) {
                            minIndex = currentIndex + 1;
                        } else if (documentsList[currentIndex] > newDocument.title) {
                            maxIndex = currentIndex - 1;
                        } else {
                            while (documentsList[currentIndex].author < newDocument.author) {
                                ++currentIndex;
                            } return currentIndex;
                        }
                    } return currentIndex;
                }

                function addDocumentOrdered(documentsList, newDocument) {
                    var index = getIndexToInsert(documentsList, newDocument);
                    documentsList.splice(index, 0, newDocument);
                }

                String.prototype.capitalizeFirstLetter = function() {
                    return this.charAt(0).toUpperCase() + this.slice(1);
                };

                function treatException(e) {
                    if (e.toString().indexOf('DocumentContentNotFoundException') !== -1) {
                        return 'EXCEPTION_DOCUMENT_CONTENT_NOT_FOUND';
                    } else if (e.toString().indexOf('InvalidDetailsException') !== -1) {
                        return 'EXCEPTION_INVALID_DETAILS';
                    } else if (e.toString().indexOf('AlreadyExistingDocumentException') !== -1) {
                        return 'EXCEPTION_ALREADY_EXISTING_DOCUMENT';
                    } else if (e.toString().indexOf('ImportExportException') !== -1) {
                        return 'EXCEPTION_IMPORT';
                    }
                }

                function showToast(toastText, error) {
                    (function() {
                        $mdToast.show(
                            $mdToast.simple()
                                .textContent($filter('translate')(toastText))
                                .theme(error?'error-toast':'success-toast')
                                .position(getToastPosition())
                                .hideDelay(3000)
                        );
                    }());

                    function getToastPosition() {
                        var positions = {
                            bottom: true,
                            top: false,
                            left: false,
                            right: true
                        };
                        return Object.keys(positions).filter(function(pos) {
                            return positions[pos];
                        }).join(' ');
                    }
                }

            }
        };
    }


}());
