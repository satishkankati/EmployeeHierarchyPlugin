/* 
 * Copyright Fortuna Identity India Private Limited. 
 * please do not copy without permission
 */
angular.module('emphierarchy').config(function($httpProvider) {
	$httpProvider.defaults.xsrfCookieName = "CSRF-TOKEN";
});
angular.module('emphierarchy').service(
		'EmpHierarchyService',
		function($http, $q, $location, REST_URL_BASE, PLUGIN_URL_BASE) {

			/**
			 * Sends an http request to the rest service
			 * 
			 * @param {type}
			 *            method string representing the type of request. (POST,
			 *            GET, etc)
			 * @param {type}
			 *            restPath path to the rest service call
			 * @param {type}
			 *            params parameters added to a 'filter' header
			 * @returns {$q@call;defer.promise} angular promise to retrieve the
			 *          data.
			 */
			function httpRequest(method, restPath, params) {
				var def = $q.defer();

				if (typeof params === 'undefined') {
					params = "";
				}

				$http({
					method : method,
					url : REST_URL_BASE + '/' + restPath,
					cache : false,
					xsrfHeaderName : 'X-XSRF-TOKEN',
					xsrfCookieName : 'CSRF-TOKEN',
					headers : {
						'params' : params,
						
					}
				}).then(function successCallBack(response) {
					console.log("#######",response)
					var d;

					// if the data returned is a string,
					// parse it into a
					// JSON object to return.
					if (response.status == 204 || response.status == 210) {

						def.resolve(response);
					} else if (typeof response.data === 'string') {
						d = JSON.parse(response.data);
					} else {
						d = response.data;
					}
					// resolves the angular promise with
					// the data (what
					// is sent
					// to the 'then' function)

					def.resolve(d);
				}, function errorCallBack(errorResponse) {
					def.reject(errorResponse);
				});
				return def.promise;
			}

			/**
			 * Sends an http request to the rest service
			 * 
			 * @param {type}
			 *            method string representing the type of request. (POST,
			 *            GET, etc)
			 * @param {type}
			 *            restPath path to the rest service call
			 * @param {type}
			 *            params parameters added to a 'filter' header
			 * @returns {$q@call;defer.promise} angular promise to retrieve the
			 *          data.
			 */
			function postHttpRequest(restPath, params) {
				if (typeof result === 'undefined') {
					result = "";
				}
				var def = $q.defer();
				return $http({
					method : 'POST',
					url : REST_URL_BASE + '/' + restPath,
					cache : false,
					xsrfHeaderName : 'X-XSRF-TOKEN',
					xsrfCookieName : 'CSRF-TOKEN',
					headers : {
						'Content-Type' : undefined
					},
					data : params,

					transformRequest : function(data, headersGetter) {
						var formData = new FormData();
						angular.forEach(data, function(value, key) {
							formData.append(key, value);
						});
						return formData;
					}
				})

			}
			this.getAttributeLabel = function() {
				return httpRequest('GET', "getAttributeLabel", null);
			}
			this.getSPCustomObjects = function(suggestUrl, query, start, limit,
					extraParams, filter) {
				var params = {
					searchValue : query,
					pageSize : limit,
					extraParams : extraParams,
					startIdx : start,
					filter : filter
				}
				return httpRequest('GET', suggestUrl, JSON.stringify(params));
			}
			this.getEmployees = function(attrValue,manager) {
				var params = {
					'attrValue' : attrValue,
					'manager' : manager
				};
				return postHttpRequest( "getEmployees", params);
			}

		});
