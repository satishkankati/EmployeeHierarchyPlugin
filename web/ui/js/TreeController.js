/**
 * Created by Satish kumar Kankati
 */

var app = angular.module('tree', [ 'tree.directives' ]);
app
		.controller(
				"TreeController",
				[
						"$http",
						"$scope",
						function($http, $scope) {
							var tc = this;
							var config = {
								headers : {
									'Content-Type' : 'application/x-www-form-urlencoded;charset=utf-8;',
									'X-XSRF-TOKEN' : PluginHelper
											.getCsrfToken()
								}
							};

							$scope.obj = {
								manager : "",
								attr : "All"
							};
							$scope.attrname=null;
							$scope.errorFlag = false;
							initAttributes();

							function initAttributes() {
								$http
										.get(
												SailPoint.CONTEXT_PATH
														+ "/plugin/rest/emphierarchy/getAttributeValues",
												config)
										.then(
												function(response) {
													console
															.error('Attribute values loaded.');

													$scope.attrname = response.data.ATTRNAME;
													$scope.attributes = response.data.ATTRVALUES;
													$scope.managers = response.data.MANAGERS;
													$scope.obj.manager = "";
													$scope.obj.attr = "All";
													buildTree();

												},
												function(errResponse) {
													$scope.errorFlag = true;
													$scope.attributes = null;
													$scope.managers = null;
													$scope.errorMessage = "Error while loading attribute values!Check the attribute name in plugin object settings";
													console
															.error('Error while loading attributes ...');
												});
							}
							;

							function buildTree() {
								tc.tree = null;
								$scope.dataLoading = true;
								$scope.errorFlag = false;

								var data = $.param({
									'attrValue' : $scope.obj.attr,
									'manager' : $scope.obj.manager
								});

								$http
										.post(
												SailPoint.CONTEXT_PATH
														+ "/plugin/rest/emphierarchy/getEmployees",
												data, config)
										.then(
												function(response) {
													console
															.error('Tree Generated..');
													$scope.dataLoading = false;
													tc.tree = response.data;
												},
												function(errResponse) {
													$scope.errorFlag = true;
													$scope.dataLoading = false;
													$scope.errorMessage = "Managers are in infinite loop";
													$scope.managerLoop = errResponse.data.message;
													console
															.error('Error while Generating Tree..');
												});
							}

							$scope.change = function() {
								buildTree();
							}
							$scope.reset = function() {
								$scope.obj.attr = "All";
								$scope.obj.manager = "";
								buildTree();
							}

						} ]);
