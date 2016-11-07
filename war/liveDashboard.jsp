<%@ include file="headerLD.jsp" %>
<% 
String action = "liveDashboard";
String hideProject = request.getAttribute("hideProject")==null?"N":(String)request.getAttribute("hideProject");
%>
<input type="hidden" name="fromScreen" id="fromScreen" value="liveDashboard.jsp"/>
<input type="hidden" name="screenTitle" id="screenTitle" value="<%=ServletConstants.LIVE_DASHBOARD%>"/>
<input type="hidden" name="prevScreen" id="prevScreen" value=""/>
<input type="hidden" name="selectedAction" id="selectedAction" value=""/>
<input type="hidden" name="selectedProject" id="selectedProject" value=""/>
<input type="hidden" name="selectedHide" id="selectedHide" value="<%=hideProject%>"/>
<input type="hidden" name="selectedFilter" id="selectedFilter" value=""/>
<script language="javascript">;

var visible = "no";

function thisScreenLoad() {	
}

function navigationAction(action) {	
	if (action=="fwd") {
		document.getElementById("toScreen").value = "<%=ServletConstants.LIVE_DASHBOARD%>";
		document.getElementById("f1").action = "navigation";
		document.getElementById("f1").submit();
	} else if (action=="rwd") {
		document.getElementById("selectedAction").value = "rewind";
		document.getElementById("toScreen").value = "<%=ServletConstants.LIVE_DASHBOARD%>";
		document.getElementById("f1").action = "liveDashboard";
		document.getElementById("f1").submit();
	} else if (action=="go") {
		var selectedProject = document.getElementById("selectProjectLD").value;
		if (selectedProject == "" ) {
			alert('No project selected for GO action');
		} else {
			document.getElementById("selectedAction").value = "go";
			document.getElementById("selectedProject").value = selectedProject;
			document.getElementById("toScreen").value = "<%=ServletConstants.LIVE_DASHBOARD%>";
			document.getElementById("f1").action = "liveDashboard";
			document.getElementById("f1").submit();
		}
	} else if (action=='hide') {
		visible = "no";
		document.getElementById("selectedAction").value = "hide";
		document.getElementById("top").style.display = "none";
		document.getElementById("siteList").style.display = "none";
		document.getElementById("siteList").style.height = "384px";
		document.getElementById("selectedHide").value = "Y";
		document.getElementById("toScreen").value = "<%=ServletConstants.LIVE_DASHBOARD%>";
		document.getElementById("f1").action = "liveDashboard";
		document.getElementById("f1").submit();
	} else if (action=='show') {
		visible = "yes";
		document.getElementById("selectedAction").value = "show";
		document.getElementById("top").style.display = "inline";
		document.getElementById("siteList").style.display = "none";
		document.getElementById("siteList").style.height = "164px";	
		document.getElementById("selectedHide").value = "N";
		document.getElementById("toScreen").value = "<%=ServletConstants.LIVE_DASHBOARD%>";
		document.getElementById("f1").action = "liveDashboard";
		document.getElementById("f1").submit();
	}
} 

function siteProgressItemsKeyClick(action) {
	var header = document.getElementById("hAnchor");
	var position = getPosition(header);
	var aR = document.getElementById("siteProgressItemsKey");
	if (action=='close') {
		aR.style.display = "none";
		aR.style.left = "0px";
		aR.style.top = "0px";
	} else {
		aR.style.display = "inline";
		aR.style.left = position.x + "px";
		aR.style.top = position.y + "px";
		aR.style.zIndex = "10";
	}
}

function siteProgressStatusKeyClick(action) {
	var header = document.getElementById("h2Anchor");
	var position = getPosition(header);
	var aR = document.getElementById("siteProgressStatusKey");
	if (action=='close') {
		aR.style.display = "none";
		aR.style.left = "0px";
		aR.style.top = "0px";
	} else {
		aR.style.display = "inline";
		aR.style.left = position.x + "px";
		aR.style.top = position.y + "px";
		aR.style.zIndex = "20";
	}
}

function projectClick(action) {
	var header = document.getElementById("h3Anchor");
	var position = getPosition(header);
	var aR = document.getElementById("chgProjectFilter");
	if (action=='close') {
		aR.style.display = "none";
		aR.style.left = "0px";
		aR.style.top = "0px";
	} else {
		aR.style.display = "inline";
		aR.style.left = position.x + "px";
		aR.style.top = position.y + "px";
		aR.style.zIndex = "30";
	}
}
</script> 
<div style="float: left;width: 1270px; margin: auto; margin-top: 10px; 
overflow-y: auto; overflow-x: hidden; border: none; height: 460x;">
<div id="top" style="height: 180px; display: <%=uB.getDisplayProject()%>;">
<table style="table-layout: fixed; border-style: none;">
<tr>
</table>
</div>
<div style="margin: 0; padding: 0; max-height: 640px; overflow; hidden;">
<table style="table-layout: fixed; border-style: none;">
<tr>
<td width="98%">
<!-- daily status section -->
<table style="height: 56px; width: 1250px; table-layout: fixed;">
<colgroup>
<col width="8%"/>
<col width="8%"/>
<col width="9%"/>
<col width="5%"/>
<col width="9%"/>
<col width="9%"/>
<col width="9%"/>
<col width="3%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
</colgroup>
<tbody>	
	<tr>
		<td  class="ldBlank" colspan="4">&nbsp;</td>
	</tr>
	<tr>
		<td class="ldTitle" colspan="6"><%=uB.GetLiveSitesHeading()%></td>
		<td id="hAnchor" class="ldBlank" colspan="3"></td>			
	</tr>
	<tr>
		<td class="ldBlank" colspan="4">&nbsp;</td>
	</tr>
	<tr>
		<td class="ldHead" rowspan="2">Customer</td>
		<td class="ldHead" rowspan="2">Partner</td>
		<td id="h3Anchor" class="ldHead" rowspan="2" 
			onClick="projectClick('open')" title="<%=uB.GetLiveSitesFilter()%>">Project</td>
		<td class="ldHead" rowspan="2">Site</td>
		<td class="ldHead" rowspan="2">BO</td>
		<td id="h2Anchor" class="ldHead" rowspan="2">FE</td>
		<td class="ldHead" rowspan="2">Overall Status</td>
		<td class="ldHead" title="Scheduled Date" rowspan="2">SD</td>
		<td class="ldHeadRightDashStress" colspan="5">Starting</td>
		<td class="ldHeadBothDashStress" colspan="8">In Progress</td>
		<td class="ldHeadBothDashStress" colspan="5">Completing</td>
		<td class="ldHeadLeftDashStress" colspan="2">Issues</td>
	</tr>
	<tr>	
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Checked In (BO)">CI</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Site Booked On (BO)">SB</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Site Accessed (FE)">SA</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Physical Checks (FE)">PC</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHeadRightDash" title="Pre Call Tests (FE)">TC</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHeadLeftDash" title="Site Locked (BO/FE)">SL</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="HW Installed (FE)">HW</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Field Commissioning (FE)">FC</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Back Office Commissioning (BO)">BC</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Tx Provisioning (Client)">Tx</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Field Work (FE)">FW</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Site Unlocked (BO)">SU</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHeadRightDash" title="Post Call Test (FE)">TC</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHeadLeftDash" title="Closure Code (BO)">CC</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Left Site (BO)">SL</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Booked Off Site (FE)">SB</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Performance Monitoring (BO)">Prf</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHeadRightDash" title="Hand Over Pack (BO)">HoP</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHeadLeftDash" title="Devoteam">Devo</td>
		<td onClick="siteProgressItemsKeyClick('open')" 
			class="ldHead" title="Vodafone">VF</td>
	</tr>
</tbody>
</table>
<div id="siteList" style="margin: 0; padding: 0; overflow-y: auto; overflow-x: hidden; display; inline; 
max-width: 100%; height: <%=(uB.getDisplayProject().equals("none")?"384":"164")%>px;"/>
<!--max-width: 100%; height: 164px"/>  -->
<table style="width: 1250px; table-layout: fixed;">
<colgroup>
<col width="8%"/>
<col width="8%"/>
<col width="9%"/>
<col width="5%"/>
<col width="9%"/>
<col width="9%"/>
<col width="9%"/>
<col width="3%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
<col width="2%"/>
</colgroup>
<tbody>	
<%=uB.getLiveDashboardSitesHTML()%>
</tbody>
</div>
</div>
</table>
</td>
</tr>
</table>
</div>
<a href="" id="aLink" name="aLink" style="display:none"></a>
<!-- includes -->
<%@ include file="siteProgressItemsKey.txt" %>
<%@ include file="siteProgressStatusKey.txt" %>
<%@ include file="chgProjectFilter.txt" %>
</form>
</body>
</html>