/**
 * Created by Satish kumar Kankati
 */
(function (ng) {
    var app = ng.module('tree', ['tree.directives']);
    app.controller("TreeController", ["$http","$scope", function ($http,$scope) {
        var tc = this
			var config = {
					headers : {
					'Content-Type' : 'application/x-www-form-urlencoded;charset=utf-8;',
					'X-XSRF-TOKEN' : PluginFramework.CsrfToken
					}
			} ;
		$scope.manager=null;
		$scope.errorFlag=false;
		initAttributes();
	    
		
		function initAttributes(){
			$http.get(SailPoint.CONTEXT_PATH+"/plugin/emphierarchyplugin/getAttributeValues",config).then(
            function (response) {
	            console.error('Attribute values loaded.');
	            $scope.attrname=response.data.ATTRNAME;
    			$scope.attrs=response.data.ATTRVALUES;
    			$scope.managers=response.data.MANAGERS;
				$scope.attr="All";
				$scope.manager=$scope.managers[0];
				buildTree();

	        },
            function(errResponse){
				$scope.errorFlag=true;
				$scope.errorMessage="Error while loading attribute values!Check the attribute name in configuration object.";
                console.error('Error while loading attributes ...');
				}
			);
        };
		
        function buildTree() {
        	tc.tree=null;
			$scope.dataLoading=true;
			$scope.errorFlag=false;
			var data = $.param({
							'attrValue' : $scope.attr,
							'manager'    : $scope.manager.key
			});

		$http.post(SailPoint.CONTEXT_PATH+"/plugin/emphierarchyplugin/getEmployees",data,config).then(
            function (response) {
	            console.error('Tree Generated..');
				$scope.dataLoading=false;
    			tc.tree=response.data;
            },
            function(errResponse){
				$scope.errorFlag=true;
				$scope.dataLoading=false;
				$scope.errorMessage="Managers are in infinite loop";
				$scope.managerLoop=errResponse.data;
		        console.error('Error while Generating Tree..');
            }
        );
        }
		
		$scope.change= function(){
			buildTree();
		}
		$scope.reset=function(){
			$scope.attr="All";
			$scope.manager=$scope.managers[0];
			buildTree();
		}
			
            
        }]
    );
})(angular);
