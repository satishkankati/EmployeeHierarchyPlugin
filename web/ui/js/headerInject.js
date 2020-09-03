var employeeUrl = SailPoint.CONTEXT_PATH + '/plugins/pluginPage.jsf?pn=emphierarchy';
var jQueryClone = jQuery;
var empstatusClass = 'healthSuccess';

jQuery(document).ready(function(){

	jQuery("ul.navbar-right li:first")
		.before(
				'<li class="dropdown">' +
				'		<a href="' + employeeUrl + '" tabindex="0" role="menuitem" data-snippet-debug="off">' +
				'			<i id="empStatusIcon" role="presenation" class="fa fa-users fa-lg ' + empstatusClass + '"></i>' +
				'		</a>' +
				'</li>'
		);
});
