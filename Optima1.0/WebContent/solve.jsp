<%@ include file="header.html"%>

 <style>
  #sortable { list-style-type: none; margin: 0; padding: 0; width: 60%; }
  #sortable li { margin: 0 3px 3px 3px; padding: 0.4em; padding-left: 1.5em; font-size: 1.0em; height: 14px; }
  #sortable li span { position: absolute; margin-left: -1.3em; }
  </style>
  <h3 class="header"><b>Project Scheduling</b></h3>
   <ul id="sortable">
		  <li class="ui-state-default" id="start"></li>
		  <li class="ui-state-default" id="end"></li>
		  <li class="ui-state-default" id="next"></li>
		</ul>
 <div id="projectWrapper" >
	<div id="solutionTab" >
	 
	 	
	<div class="form-section">
	   
	 	<div class="two-columns">
	 	<label for="currentProject">Project</label>
	 	<select name="currentProject"
				id="currentProject"
				class="custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-all">
		</select>
	 	</div>
		 <div class="two-columns">
		   <label for="extraCash">Extra Cash</label>
			<Input type="text" name="extraCash"
					id="extraCash"
					 readonly="readonly" />
		 </div>
	 </div>
	  <br>
	
	   
	 <div class="form-section">
	 <h3>Cost Current Period </h3>
	 <br>
		<div class="form-section">
		<div id="costCurrentPeriodGrid" style="width:1024px; height:100px;"></div>
		</div>
	 </div>
	 
	 <div class="form-section">
	 <h3>Cashout Next Period (Portfolio)</h3>
	 <br>
		<div class="form-section">
		<div id="cashOutNextPeriodGrid" style="width:1024px; height:150px;"></div>
		</div>
	 </div>
	 
	 <div class="form-section">
	 <h3>Extra Cash Next Period (Portfolio)</h3>
	 <br>
		<div class="form-section">
		<div id="extraCashtNextPeriodGrid" style="width:1024px; height:150px;"></div>
		</div>
	 </div>
	 
	 <div class="form-section">

		 <div class="two-columns-right">
			 <div id="OutputFormat">
				<input type="radio"  id="detailedOutput" name="output" /><label for="detailedOutput">Detailed Output</label>
				<input type="radio"  id="simpleOutput" name="output" checked="checked"/><label for="simpleOutput">Simple Output</label>
			</div>
		     
			<button id="generateSolution" >Solve</button>
		 </div>
	 </div>
   </div>
	
 </div>

<div id="dialog-form" title="Solution">
  <p class="validateTips">Solution Schedule</p>
  <div id="solutionGrid" style="width:600px; height:400px;"></div>
 
  
</div>
	  <script src="js/formutils.js"> </script> 
	  <script src="js/solve.js"> </script> <!--  all page specific js goes here -->
	  
	

<%@ include file="footer.html"%>