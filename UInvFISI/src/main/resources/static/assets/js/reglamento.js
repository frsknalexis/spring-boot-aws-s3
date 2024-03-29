$(document).on('ready', function() {
	
	
	$('#fileReglamento').on('change', function() {
		
		var fileInput = $('#fileReglamento');
		
		var file = $('#fileReglamento').val();
		
		console.log("file: " + file);
		
		var extensionesPermitidas = new Array(".pdf", ".doc", ".docx", ".xls", ".xlsx");
		
		var extension = (file.substring(file.lastIndexOf("."))).toLowerCase();
		
		var permitido = false;
		
		for(var i = 0; i < extensionesPermitidas.length; i++) {
			
			if(extensionesPermitidas[i] == extension) {
				permitido = true;
				break;
			}
		}
		
		if(!permitido) {
			
			swal({
                type: 'error',
                title: 'Ooops',
                text: 'Asegurate de haber selecciondo un archivo permitido'
            });
			
			fileInput.val("");
			return false;
		}
		
		else if(permitido) {
			
			fileReglamentoPreview(this);
		}		
	});
	
	function fileReglamentoPreview(fileInput) {
		
		if (fileInput.files && fileInput.files[0]) {
            var reader = new FileReader();
            reader.onload = function(e) {
            	$('#visorArchivoReglamento').html('<embed src="' + e.target.result + '" width="100%" height="800">');
            };
            reader.readAsDataURL(fileInput.files[0]);
        }
	}
	
	$('#guardarReglamento').click(function(e) {
		
		e.preventDefault();
		
		if($('#asuntoReglamento').val().match(/^[a-zA-Z0-9ñÑáéíóúÁÉÍÓÚ\-\s]+$/)) {
			
			var form = $('#formReglamento')[0];
			var data = new FormData(form);
			
			var formData = {
					reglamentoId: $('#reglamentoId').val(),
					asunto: $('#asuntoReglamento').val()
			};
			
			console.log(formData);
			data.append("reglamentoJson", JSON.stringify(formData));
			
			if(formData.reglamentoId) {
				
				reglamentoId = formData.reglamentoId;
				console.log("reglamentoId: " + reglamentoId);
				
				$.ajax({
					
					type: 'PUT',
					url: '/api/reglamento/update/' + reglamentoId,
					enctype: 'multipart/form-data',
					data: data,
					processData: false,
					contentType: false,
					success: function(response) {
						
						console.log(response);
						
						swal({
							type: "success",
							title: "Reglamento Actualizado con exito",
							showConfirmButton: true,
							confirmButtonText: "Cerrar",
							closeOnConfirm: false
						}).then((result) => {

							if(result.value) {
								$(location).attr('href', '/reglamento/listar');
							}
						});
					},
					error: function() {
						alert('Error al Actualizar Reglamento');
					}
				});
			}
			else {
				
				$.ajax({
					
					type: 'POST',
					url: '/api/reglamento/save',
					enctype: 'multipart/form-data',
					data: data,
					processData: false,
					contentType: false,
					success: function(response) {
						
						console.log(response);
						
						swal({
							type: "success",
							title: "Reglamento Registrado con exito",
							showConfirmButton: true,
							confirmButtonText: "Cerrar",
							closeOnConfirm: false
						}).then((result) => {

							if(result.value) {
								$(location).attr('href', '/reglamento/listar');
							}
						});
					},
					error: function() {
						alert('Error al Registrar Reglamento');
					}
				});
			}
		}
		
		if($('#asuntoReglamento').val() == "" || $('#asuntoReglamento').val() == 0) {
			
			swal({
                type: 'error',
                title: 'Ooops',
                text: 'El campo Descripcion no puede estar vacio, ingrese un valor valido'
            });
	    	
	    	$('#asuntoReglamento').focus();
	    	return false;
			
		}
		
		else {
			
			if(!($('#asuntoReglamento').val().match(/^[a-zA-Z0-9ñÑáéíóúÁÉÍÓÚ\-\s]+$/))) {
				
				swal({
	                type: 'error',
	                title: 'Ooops',
	                text: 'El campo Descripcion no permite caracteres especiales'
	            });
				
				$('#asuntoReglamento').focus();
		    	return false;
			}
		}
	});
	
	$('#dataTable tbody').on('click', 'button#editarReglamento', function() {
		
		var reglamentoId = $(this).attr('idreglamento');
		console.log("reglamentoId: " + reglamentoId);
		$(location).attr('href', '/reglamento/form/' + reglamentoId);
	});
	
	$('#dataTable tbody').on('click', 'button#downloadReglamento', function() {
		
		var nombreFicheroReglamento = $(this).attr('nombreficheroreglamento');
		console.log("nombreFicheroReglamento: " + nombreFicheroReglamento);
		
		$(location).attr('href', '/api/reglamento/download/' + nombreFicheroReglamento);
	});
});