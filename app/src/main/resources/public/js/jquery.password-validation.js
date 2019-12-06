(function($) {
	$.fn.extend({
		passwordValidation: function(_options, _callback, _confirmcallback) {
			//var _unicodeSpecialSet = "^\\x00-\\x1F\\x7F\\x80-\\x9F0-9A-Za-z"; //All chars other than above (and C0/C1)
			var CHARSETS = {
				upperCaseSet: "A-Z", 	//All UpperCase (Acii/Unicode)
				lowerCaseSet: "a-z", 	//All LowerCase (Acii/Unicode)
				digitSet: "0-9", 		//All digits (Acii/Unicode)
				specialSet: "\\x20-\\x2F\\x3A-\\x40\\x5B-\\x60\\x7B-\\x7E\\x80-\\xFF", //All Other printable Ascii
			}
			var _defaults = {
				/*minLength: 12,		  //Minimum Length of password
				minUpperCase: 2,	  //Minimum number of Upper Case Letters characters in password
				minLowerCase: 2,	  //Minimum number of Lower Case Letters characters in password
				minDigits: 2,		  //Minimum number of digits characters in password
				minSpecial: 2,		  //Minimum number of special characters in password
				maxRepeats: 5,		  //Maximum number of repeated alphanumeric characters in password dhgurAAAfjewd <- 3 A's
				maxConsecutive: 3,	  //Maximum number of alphanumeric characters from one set back to back
				noUpper: false,		  //Disallow Upper Case Lettera
				noLower: false,		  //Disallow Lower Case Letters
				noDigit: false,		  //Disallow Digits
				noSpecial: false,	  //Disallow Special Characters
				//NOT IMPLEMENTED YET allowUnicode: false,  //Switches Ascii Special Set out for Unicode Special Set
				failRepeats: true,    //Disallow user to have x number of repeated alphanumeric characters ex.. ..A..a..A.. <- fails if maxRepeats <= 3 CASE INSENSITIVE
				failConsecutive: true,//Disallow user to have x number of consecutive alphanumeric characters from any set ex.. abc <- fails if maxConsecutive <= 3
				confirmField: undefined*/
        minLength: 8,		  //Minimum Length of password
        minUpperCase: 2,	  //Minimum number of Upper Case Letters characters in password
        minLowerCase: 2,	  //Minimum number of Lower Case Letters characters in password
        minDigits: 2,
			};

			//Ensure parameters are correctly defined
			if($.isFunction(_options)) {
				if($.isFunction(_callback)) {
					if($.isFunction(_confirmcallback)) {
						console.log("Warning in passValidate: 3 or more callbacks were defined... First two will be used.");
					}
					_confirmcallback = _callback;
				}
				_callback = _options;
				_options = {};
			}

			//concatenate user options with _defaults
			_options = $.extend(_defaults, _options);
			if(_options.maxRepeats < 2) _options.maxRepeats = 2;

			function charsetToString() {
				return CHARSETS.upperCaseSet + CHARSETS.lowerCaseSet + CHARSETS.digitSet + CHARSETS.specialSet;
			}

			//GENERATE ALL REGEXs FOR EVERY CASE
		/*	function buildPasswordRegex() {
				var cases = [];

				//if(_options.allowUnicode) CHARSETS.specialSet = _unicodeSpecialSet;
				if(_options.noUpper) 	cases.push({"regex": "(?=" + CHARSETS.upperCaseSet + ")",  																				"message": "Password can't contain an Upper Case Letter"});
				else 					cases.push({"regex": "(?=" + ("[" + CHARSETS.upperCaseSet + "][^" + CHARSETS.upperCaseSet + "]*").repeat(_options.minUpperCase) + ")", 	"message": "Password must contain at least " + _options.minUpperCase + " Upper Case Letters."});
				if(_options.noLower) 	cases.push({"regex": "(?=" + CHARSETS.lowerCaseSet + ")",  																				"message": "Password can't contain a Lower Case Letter"});
				else 					cases.push({"regex": "(?=" + ("[" + CHARSETS.lowerCaseSet + "][^" + CHARSETS.lowerCaseSet + "]*").repeat(_options.minLowerCase) + ")", 	"message": "Password must contain at least " + _options.minLowerCase + " Lower Case Letters."});
				if(_options.noDigit) 	cases.push({"regex": "(?=" + CHARSETS.digitSet + ")", 																					"message": "Password can't contain a Number"});
				else 					cases.push({"regex": "(?=" + ("[" + CHARSETS.digitSet + "][^" + CHARSETS.digitSet + "]*").repeat(_options.minDigits) + ")", 			"message": "Password must contain at least " + _options.minDigits + " Digits."});
				if(_options.noSpecial) 	cases.push({"regex": "(?=" + CHARSETS.specialSet + ")", 																				"message": "Password can't contain a Special Character"});
				else 					cases.push({"regex": "(?=" + ("[" + CHARSETS.specialSet + "][^" + CHARSETS.specialSet + "]*").repeat(_options.minSpecial) + ")", 		"message": "Password must contain at least " + _options.minSpecial + " Special Characters."});

				cases.push({"regex":"[" + charsetToString() + "]{" + _options.minLength + ",}", "message":"Password must contain at least " + _options.minLength + " characters."});

				return cases;
			}*/

      	function buildPasswordRegex() {
                var cases = [];

                //if(_options.allowUnicode) CHARSETS.specialSet = _unicodeSpecialSet;
                if(_options.noUpper) 	cases.push({"regex": "(?=" + CHARSETS.upperCaseSet + ")",  																				"message": "Password can't contain an Upper Case Letter"});
                else 					cases.push({"regex": "(?=" + ("[" + CHARSETS.upperCaseSet + "][^" + CHARSETS.upperCaseSet + "]*").repeat(_options.minUpperCase) + ")", 	"message": "doit contenir au moins " + _options.minUpperCase + " Majuscules."});
                if(_options.noLower) 	cases.push({"regex": "(?=" + CHARSETS.lowerCaseSet + ")",  																				"message": "Password can't contain a Lower Case Letter"});
                else 					cases.push({"regex": "(?=" + ("[" + CHARSETS.lowerCaseSet + "][^" + CHARSETS.lowerCaseSet + "]*").repeat(_options.minLowerCase) + ")", 	"message": "doit contenir au moins " + _options.minLowerCase + " Minuscules."});
                if(_options.noDigit) 	cases.push({"regex": "(?=" + CHARSETS.digitSet + ")", 																					"message": "Password can't contain a Number"});
                else 					cases.push({"regex": "(?=" + ("[" + CHARSETS.digitSet + "][^" + CHARSETS.digitSet + "]*").repeat(_options.minDigits) + ")", 			"message": "doit contenir au moins " + _options.minDigits + " Chiffres."});
                if(_options.noSpecial) 	cases.push({"regex": "(?=" + CHARSETS.specialSet + ")", 																				"message": "Password can't contain a Special Character"});
                else 					cases.push({"regex": "(?=" + ("[" + CHARSETS.specialSet + "][^" + CHARSETS.specialSet + "]*").repeat(_options.minSpecial) + ")", 		"message": "Password must contain at least " + _options.minSpecial + " Special Characters."});

                cases.push({"regex":"[" + charsetToString() + "]{" + _options.minLength + ",}", "message":"doit contenir au moins " + _options.minLength + " caractères."});

                return cases;
            }
			var _cases = buildPasswordRegex();

			var _element = this;
			var $confirmField = (_options.confirmField != undefined)? $(_options.confirmField): undefined;

			//Field validation on every captured event
			function validateField() {
				var failedCases = [];

				//Evaluate all verbose cases
				$.each(_cases, function(i, _case) {
					if($(_element).val().search(new RegExp(_case.regex, "g")) == -1) {
						failedCases.push(_case.message);
					}
				});
				if(_options.failRepeats && $(_element).val().search(new RegExp("(.)" + (".*\\1").repeat(_options.maxRepeats - 1), "gi")) != -1) {
					failedCases.push("Password can not contain " + _options.maxRepeats + " of the same character case insensitive.");
				}
				if(_options.failConsecutive && $(_element).val().search(new RegExp("(?=(.)" + ("\\1").repeat(_options.maxConsecutive) + ")", "g")) != -1) {
					failedCases.push("Password can't contain the same character more than " + _options.maxConsecutive + " times in a row.");
				}

				//Determine if valid
				var validPassword = (failedCases.length == 0) && ($(_element).val().length >= _options.minLength);
				var fieldsMatch = true;
				if($confirmField != undefined) {
					fieldsMatch = ($confirmField.val() == $(_element).val());
				}

				_callback(_element, validPassword, validPassword && fieldsMatch, failedCases);
			}

			//Add custom classes to fields
			this.each(function() {
				//Validate field if it is already filled
				if($(this).val()) {
					validateField().apply(this);
				}

				$(this).toggleClass("jqPassField", true);
				if($confirmField != undefined) {
					$confirmField.toggleClass("jqPassConfirmField", true);
				}
			});

			//Add event bindings to the password fields
			return this.each(function() {
				$(this).bind('keyup focus input proprtychange mouseup', validateField);
				if($confirmField != undefined) {
					$confirmField.bind('keyup focus input proprtychange mouseup', validateField);
				}
			});
		}
	});
})(jQuery);