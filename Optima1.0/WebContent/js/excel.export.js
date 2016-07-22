/*
 * jQuery Excel Export Plugin Library
 * http://tarunbatta.blogspot.com/
 *
 * Copyright (c) 2013 Tarun Batta
 * Licensed under BTechCo licenses.
 * https://github.com/btechco/btechco_excelexport/wiki
 *
 */

(function($) {

	$datatype = {
		Table : 1,
		Json : 2,
		Xml : 3,
		JqGrid : 4
	}

	var $defaults = {
		containerid : null,
		datatype : $datatype.Table,
		dataset : null,
		columns : null
	};

	var $settings = $defaults;

	$.fn.btechco_excelexport = function(options) {
		$settings = $.extend({}, $defaults, options);

		switch ($settings.datatype) {
		case 1:
			Export($("#" + $settings.containerid).parent().html());
			break;
		case 2:			
			//Export(ConvertJsonToTable());
			ExportJsonToExcel();
			break;
		case 3:
			Export(ConvertXmlToTable());
			break;
		case 4:
			Export(ConvertJqGridDataToTable());
			break;
		}

		function ConvertJsonToTable() {
			var result = "<table>";

			result += "<thead><tr>";
			$($settings.columns).each(function(key, value) {
				if (this.ishidden != true) {
					result += "<th";
					if (this.width != null) {
						result += " style='width: " + this.width + "'";
					}
					result += ">";
					result += this.headertext;
					result += "</th>";
				}
			});
			result += "</tr></thead>";

			result += "<tbody>";
			$($settings.dataset).each(function(key, value) {
				// console.log("key: " + key + ", value: " + value.toSource());
				result += "<tr>";
				$($settings.columns).each(function(k, v) {
					if (value.hasOwnProperty(this.datafield)) {
						if (this.ishidden != true) {
							var date = month = day = year = '';
							result += "<td";
							if (this.width != null) {
								result += " style='width: " + this.width + "'";
							}
							result += ">";
							if (this.datafield == "tentativeStartDate") {
								date = utcTime2LocalDate(value[this.datafield].time);
								result += date.toLocaleDateString("en-CA");
							}
							if (this.datafield == "project") {
								result += value[this.datafield].projectCode;
							} else
								result += value[this.datafield];
							result += "</td>";

						}
					}
				});
				result += "</tr>";
			});
			result += "</tbody>";

			result += "</table>";
			return result;
		}

		function ConvertXmlToTable() {
			var result = "<table>";

			result += "<thead><tr>";
			$($settings.columns).each(function(key, value) {
				if (this.ishidden != true) {
					result += "<th";
					if (this.width != null) {
						result += " style='width: " + this.width + "'";
					}
					result += ">";
					result += this.headertext;
					result += "</th>";
				}
			});
			result += "</tr></thead>";

			result += "<tbody>";
			$($settings.dataset).find("row").each(function(key, value) {
				result += "<tr>";
				$($settings.columns).each(function(k, v) {
					if ($(value).attr(this.datafield)) {
						if (this.ishidden != true) {
							result += "<td";
							if (this.width != null) {
								result += " style='width: " + this.width + "'";
							}
							result += ">";
							result += $(value).attr(this.datafield);
							result += "</td>";
						}
					}
				});
				result += "</tr>";
			});
			result += "</tbody>";

			result += "</table>";
			return result;
		}

		function ConvertJqGridDataToTable() {
			var result = "<table>";

			result += "<thead><tr>";
			$($settings.columns).each(function(key, value) {
				if (this.ishidden != true) {
					result += "<th";
					if (this.width != null) {
						result += " style='width: " + this.width + "'";
					}
					result += ">";
					result += this.headertext;
					result += "</th>";
				}
			});
			result += "</tr></thead>";
			result += "<tbody>";

			$($settings.dataset).find("rows > row").each(function(key, value) {
				result += "<tr>";
				$($settings.columns).each(function(k, v) {
					if ($(value).find(this.datafield)) {
						if (this.ishidden != true) {
							result += "<td";
							if (this.width != null) {
								result += " style='width: " + this.width + "'";
							}
							result += ">";
							result += $(value).find(this.datafield).text();
							result += "</td>";
						}
					}
				});
				result += "</tr>";
			});
			result += "</tbody>";

			result += "</table>";
			return result;
		}
		
		
		
		function ExportJsonToExcel(){
			var d = new Date();
			var excelFile = '<?xml version="1.0"?> \
			<?mso-application progid="Excel.Sheet"?> \
			<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet" xmlns:o="urn:schemas-microsoft-com:office:office" \
			xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet" \
			xmlns:html="http://www.w3.org/TR/REC-html40"> \
			<DocumentProperties xmlns="urn:schemas-microsoft-com:office:office"> \
			<LastAuthor>Optima App</LastAuthor> \
			<Created>' + d.toLocaleString("en-CA") + '</Created> \
			<Version>11.9999</Version> \
			</DocumentProperties> \
			<ExcelWorkbook xmlns="urn:schemas-microsoft-com:office:excel"> \
			<WindowHeight>8190</WindowHeight> \
			<WindowWidth>16380</WindowWidth> \
			<WindowTopX>0</WindowTopX> \
			<WindowTopY>0</WindowTopY> \
			<TabRatio>344</TabRatio> \
			<ProtectStructure>False</ProtectStructure> \
			<ProtectWindows>False</ProtectWindows> \
			</ExcelWorkbook> \
			<Styles> \
			  <Style ss:ID="Default" ss:Name="Normal"> \
			   <Alignment ss:Vertical="Bottom"/> \
			   <Borders/> \
			   <Font ss:FontName="Arial" x:Family="Swiss"/> \
			   <Interior/> \
			   <NumberFormat/> \
			   <Protection/> \
			  </Style> \
			  <Style ss:ID="s21"> \
			   <NumberFormat ss:Format="yyyy\-mm\-dd"/> \
			  </Style> \
			  <Style ss:ID="s22"> \
			   <NumberFormat ss:Format="Standard" /> \
				<Alignment ss:Vertical="Center" ss:WrapText="1"/> \
			  </Style> \
			  <Style ss:ID="s23"> \
			   <Font ss:FontName="Arial" x:Family="Swiss" ss:Bold="1" ss:Underline="Single"/> \
			   <NumberFormat ss:Format="yyyy\-mm\-dd"/> \
			  </Style> \
			  <Style ss:ID="s24"> \
			   <Font ss:FontName="Arial" x:Family="Swiss" ss:Bold="1" ss:Underline="Single"/> \
			   <NumberFormat ss:Format="Standard"/> \
			  </Style> \
			  <Style ss:ID="s25"> \
			   <Font ss:FontName="Arial" x:Family="Swiss" ss:Size="14" ss:Bold="1" ss:Underline="Single"/> \
			  </Style> \
			  <Style ss:ID="currency"> \
				<NumberFormat ss:Format="Currency"/> \
			  </Style> \
			  <Style ss:ID="shortDate"> \
				 <NumberFormat ss:Format="dd\-mm\-yyyy" /> \
			  </Style> \
			 </Styles>';
			/*for(var i=0; i<$settings.dataset.length; i++){
				excelFile += CreateWorkSheet($settings.dataset[i]);
			}*/
			
			excelFile += CreateWorkSheet($settings.dataset[1]);
			excelFile += '</Workbook>';
			
			var base64data = "base64," + utf8_to_b64(excelFile);
            window.open('data:application/vnd.ms-excel;filename=test;' + base64data);
		}
		
		function utf8_to_b64( str ) {
			  return window.btoa(unescape(encodeURIComponent( str )));
			}

		
		function CreateWorkSheet(tableObj){
			var wsTemp = '<Worksheet ss:Name="' + tableObj[0].name + '">';
			var cCount = tableObj[0].columns.length;
			var rCount = 0;
			for(var i = 0; i<tableObj.length; i++)
				rCount += tableObj[i].data.length;
			
			//var rCount = tableObj[0].data.length;
			
			var colXml = '';
			wsTemp += '<Table ss:ExpandedColumnCount="256" ss:ExpandedRowCount="19" x:FullColumns="1"  x:FullRows="1" ss:DefaultColumnWidth="120"> ';
			
			
			$(tableObj[0].columns).each(function(key, value) {
				if (this.ishidden != true) {
					var _with = this.width != null? this.width:'150';
					wsTemp += '<Column ss:StyleID="s22" ss:AutoFitWidth="0" ss:Width="'+ _with +'"/>';
				}
			});
			
			//2 empty rows before the header
			for(var i=0; i<2; i++){
				wsTemp +='<Row ss:StyleID="s25">';
				$(tableObj[0].columns).each(function(key, value) {
					wsTemp +='<Cell ss:StyleID="s23"/>';
				});
				
				wsTemp +='</Row>';
			
			}
			
			//header row
			
			wsTemp += '<Row ss:StyleID="s25">' ;
            
			$(tableObj[0].columns).each(function(key, value) {//coulmn headers
				if (this.ishidden != true) {
					var hText = this.headertext;
					wsTemp += '<Cell><Data ss:Type="String">' + hText + '</Data></Cell>';
				};
			});
			wsTemp +='</Row>';
			
			//one empty row before data
			wsTemp +='<Row ss:StyleID="s25">';
			$(tableObj[0].columns).each(function(key, value) {
				wsTemp +='<Cell ss:StyleID="s23"/>';
			});
			
			wsTemp +='</Row>';
		   
			//Data
			var row;
		   wsTemp +='';
		   var fmt = new DateFmt("%w %d-%n-%y");
		   for(var i=0; i<tableObj.length;i++){
		       console.log(tableObj[i]);
		       var projCode = tableObj[i].projectCode;
		       var portfolioName = tableObj[i].portfolioName;
		   $(tableObj[i].data).each(function(dkey, dvalue) {//coulmn headers
		       
			    wsTemp += '<Row>';
			   $(tableObj[0].columns).each(function(ckey, cvalue) {//coulmn headers
					if (this.ishidden != true) {
						var cText=  dvalue[this.datafield];
						var dType =  "String";
						var cellFormat = ' ss:StyleID="s22"';
						
						if(this.datafield == "projectCode"){
							cText = projCode;
						}else if(this.datafield == "portfolioName"){
							cText =  dvalue["portfolio"].portfolioName;
						}else if(this.datafield == "paymentType"){
							cText = dvalue[this.datafield].paymentType;
						}else if(this.datafield == "asDependent"){
							var dlist = dvalue[this.datafield].list;
							//console.log(dlist[0].toSource());
							cText = '';
							for(var j = 0; j<dlist.length; j++){
								cText +=  dlist[j].dependency.taskName + ', ';
								//cText += dependent.taskName + ', ';
							}
							
							
						}else if(this.datafield.indexOf("Amount") >= 0) {
							cellFormat = ' ss:StyleID="currency"';
							dType = "Number";
							cText = dvalue[this.datafield];
						}
						else if(this.datafield.indexOf("Date") >= 0) {
							
							var d = utcTime2LocalDate(dvalue[this.datafield].time);
							//cText = d.toDateString();
							var month = (d.getMonth() + 1)<10? '0' + (d.getMonth() + 1): d.getMonth() + 1;
							cText = d.getFullYear() + "-" + month + "-" + d.getDate();// + 'T00:00:00.000Z';					
							cellFormat = ' ss:StyleID="shortDate"';
							//dType = "DateTime";
							//alert(cText);
							
						}
						wsTemp += '<Cell ' + cellFormat + '><Data ss:Type="' + dType + '">' + cText + '</Data></Cell>';
						
						//alert(cell);
					};
				});
			  wsTemp += '</Row>';
			   
			});
		   }
		  
		   
		   wsTemp +='</Table> \
			   <WorksheetOptions xmlns="urn:schemas-microsoft-com:office:excel"> \
			   <PageSetup> \
				<Layout x:StartPageNumber="1"/> \
				<Header x:Margin="0.78749999999999998" x:Data="&amp;C&amp;A"/> \
				<Footer x:Margin="0.78749999999999998" x:Data="&amp;CPage &amp;P"/> \
				<PageMargins x:Bottom="1.0249999999999999" x:Left="0.78749999999999998" \
				 x:Right="0.78749999999999998" x:Top="1.0249999999999999"/> \
			   </PageSetup> \
			   <Print> \
				<ValidPrinterInfo/> \
				<PaperSizeIndex>9</PaperSizeIndex> \
				<HorizontalResolution>300</HorizontalResolution> \
				<VerticalResolution>300</VerticalResolution> \
			   </Print> \
			   <Selected/> \
			   <ProtectObjects>False</ProtectObjects> \
			   <ProtectScenarios>False</ProtectScenarios> \
			  </WorksheetOptions> \
				</Worksheet> ';
		   
		  // console.log(wsTemp);
		   
		   return wsTemp;
		   
		}

		function Export(htmltable) {
			var excelFile = "<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:x='urn:schemas-microsoft-com:office:excel' xmlns='http://www.w3.org/TR/REC-html40'>";
			excelFile += "<head>";
			excelFile += "<!--[if gte mso 9]>";
			excelFile += "<xml>";
			excelFile += "<x:ExcelWorkbook>";
			excelFile += "<x:ExcelWorksheets>";
			excelFile += "<x:ExcelWorksheet>";
			excelFile += "<x:Name>";
			excelFile += "{Sheet1}";
			excelFile += "</x:Name>";
		//	excelFile += "<x:Table>";
		//	excelFile += htmltable.replace(/"/g, '\'');
		//	excelFile += "</x:Table>";
			excelFile += "<x:WorksheetOptions>";
			excelFile += "<x:DisplayGridlines/>";
			excelFile += "</x:WorksheetOptions>";
			//excelFile += "<x:Table>{{ROWS}}</x:Table>";
			excelFile += "</x:ExcelWorksheet>";
			excelFile += "<x:ExcelWorksheet>";
			excelFile += "<x:Name>";
			excelFile += "{Sheet2}";
			excelFile += "</x:Name>";

			excelFile += '{table}';
			excelFile += "<x:WorksheetOptions>";
			excelFile += "<x:DisplayGridlines/>";
			excelFile += "</x:WorksheetOptions>";			
			excelFile += "</x:ExcelWorksheet>";
			excelFile += "</x:ExcelWorksheets>";
			excelFile += "</x:ExcelWorkbook>";
			excelFile += "</xml>";
			excelFile += "<![endif]-->";
			excelFile += "</head>";
			excelFile += "<body>";
			//excelFile += htmltable.replace(/"/g, '\'');
			excelFile += "</body>";
			excelFile += "</html>";
			
			var base64 = function(s) {
                /* while (s.indexOf('â') != -1) s = s.replace('â','a');
                 while (s.indexOf('ş') != -1) s = s.replace('ş','s');
                 while (s.indexOf('ă') != -1) s = s.replace('ă','a');
                 while (s.indexOf('ţ') != -1) s = s.replace('ţ','t');*/
                 return window.btoa(unescape(encodeURIComponent(s)))
             }
	     , format = function(s, c) {
	                 return s.replace(/{(\w+)}/g, function(m, p) { return c[p]; });
	             };
	             
	           //  $(document).append('<a id="dlink" style="display=none" />');
	     
	             var ctx = {worksheet: 'Worksheet', table: htmltable};//table.innerHTML}
              /*   $("#dlink").attr('href', 'data:application/vnd.ms-excel;charset=UTF-8;base64,' + base64(format(excelFile, ctx)));
                 $("#dlink").attr('download', 'portfolio.xls');
                 $("#dlink").trigger( "click" );*/

			var base64data = "base64," + $.base64.encode(excelFile);
			window.open('data:application/vnd.ms-excel;charset=UTF-8;base64,'
					 + base64(format(excelFile, ctx)));
		}

	/*	var tableToExcel = (function() {
			var uri = 'data:application/vnd.ms-excel;base64,', 
			template = '<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40"><head><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet><x:Name>{worksheet}</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]--></head><body><table>{table}</table></body></html>', base64 = function(
					s) {
				return window.btoa(unescape(encodeURIComponent(s)))
			}, format = function(s, c) {
				return s.replace(/{(\w+)}/g, function(m, p) {
					return c[p];
				})
			}
			return function(table, name, filename) {
				if (!table.nodeType)
					table = document.getElementById(table)
				var ctx = {
					worksheet : name || 'Worksheet',
					table : table.innerHTML
				}

				document.getElementById("dlink").href = uri
						+ base64(format(template, ctx));
				document.getElementById("dlink").download = filename;
				document.getElementById("dlink").click();

			}
		})()*/
	};
})(jQuery);