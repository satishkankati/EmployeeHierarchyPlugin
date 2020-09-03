/**
 * Created by Satish kumar Kankati
 */
var hierarchyApp = angular.module('emphierarchy', [ 'tree.directives','ui.bootstrap',
    'ui.bootstrap.popover', 'sailpoint.widget',
	'ngSanitize',  
	'sailpoint.i18n',
	'sailpoint.util','sailpoint.dataview', 'sailpoint.config']);

// constant for the module for the URL to where the plugin files are stored
hierarchyApp.constant('PLUGIN_URL_BASE', SailPoint.CONTEXT_PATH + '/plugin/emphierarchy');

// constant for the module for the plugin REST URL
hierarchyApp.constant('REST_URL_BASE', SailPoint.CONTEXT_PATH + '/plugin/rest/emphierarchy');

hierarchyApp.factory('SPCustomObjectService', function(EmpHierarchyService){
	return {
    	getObjects: function (suggestUrl,query, start, limit, extraParams, filter) {
			return EmpHierarchyService.getSPCustomObjects(suggestUrl,query, start, limit, extraParams, filter);
    	},
		getExtraParams: function (scope) {
			return scope.extraParams;
		}
    };
});
hierarchyApp.controller("TreeController",function($scope, $location,EmpHierarchyService,SPCustomObjectService,PLUGIN_URL_BASE) {
var vm=this;
vm.attributeLabel="";
this.SPCustomObjectService=SPCustomObjectService;
this.obj = {
	manager : { displayName: "All",id:"All",name:"All"},
	attr :    { displayName: "All",id:"All",name:"All"}
};
this.initLabel = function() {
	EmpHierarchyService.getAttributeLabel().then(function (response){
		console.log("%%%%%%",response)
		vm.attributeLabel=response.ATTRNAME;
	},
	function(errResponse) {
	vm.attributeLabel = "Attribute"
	})
}

this.buildTree = function() {
		vm.tree = null;
		vm.dataLoading = false;
		vm.errorFlag = false;
		EmpHierarchyService.getEmployees(vm.obj.attr.name,vm.obj.manager.name)
				.then(
						function(response) {
							vm.dataLoading = true;
							vm.tree = response.data;
						},
						function(errResponse) {
							vm.errorFlag = true;
							vm.dataLoading = true;
							vm.errorMessage = "Managers are in infinite loop";
							vm.managerLoop = errResponse.data.message;

						});
	}

	this.change = function() {
		this.buildTree();
	}
	this.reset = function() {
		this.obj = {
				manager : { displayName: "All",id:"All",name:"All"},
				attr :    { displayName: "All",id:"All",name:"All"}
		};
		this.buildTree();	
	} 
	this.initLabel();
	this.buildTree();	
});
