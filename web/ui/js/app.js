
/* This is where you defined your app. Something like:*/
  var myApp=angular.module('tree', []);
  alert(myApp);
   myApp.controller('TreeController', ($scope) => {
	   console.log('Controller');
       $scope.greeting = "Hello World";
   });

 
