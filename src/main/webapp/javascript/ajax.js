

function toggle() {

	
	if (document.Form1.Action.value == 'Post' ) {
		return;
	}
	
	visi('added_by');
	visi('updated_by');
	
	if (showSubmit == true) {
		visi('submittedby');
	}
	
	if (showReview == true) {
		visi('reviewedby');
	}
		
}


function auditInit() {

	if (showSubmit == false) {
		visi('submittedby');
	}
	
	if (showReview == false) {
		visi('reviewedby');
	}
}



function visi(nr)
{
	if (document.layers)
	{
		vista = (document.layers[nr].visibility == 'hide') ? 'show' : 'hide'
		document.layers[nr].visibility = vista;
	}
	else if (document.all)
	{
		vista = (document.all[nr].style.visibility == 'hidden') ? 'visible'	: 'hidden';
		document.all[nr].style.visibility = vista;
	}
	else if (document.getElementById)
	{
		vista = (document.getElementById(nr).style.visibility == 'hidden') ? 'visible' : 'hidden';
		document.getElementById(nr).style.visibility = vista;

	}
}

function blocking(nr)
{
	if (document.layers)
	{
		current = (document.layers[nr].display == 'none') ? 'block' : 'none';
		document.layers[nr].display = current;
	}
	else if (document.all)
	{
		current = (document.all[nr].style.display == 'none') ? 'block' : 'none';
		document.all[nr].style.display = current;
	}
	else if (document.getElementById)
	{
		vista = (document.getElementById(nr).style.display == 'none') ? 'block' : 'none';
		document.getElementById(nr).style.display = vista;
	}
}
