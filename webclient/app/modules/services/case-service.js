/*
 *
 * Copyright 2009-2012 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function () {
  'use strict';


  var sfServices = angular.module('sf.services.case', ['sf.services.backend', 'sf.services.navigation', 'sf.models', 'sf.services.forms']);

  sfServices.factory('caseService', ['$rootScope','backendService', 'navigationService', 'SfCase', '$http', 'debounce', 'formMapperService', function ($rootScope, backendService, navigationService, SfCase, $http, debounce, formMapper) {

    var caseBase = function(caseId){
     return [
        {resources:'workspacev2'},
        {resources: 'cases', unsafe: true},
        {resources: caseId, unsafe: true}
      ];
    };
    //caseBase.bcMessage = null;
    //TODO: Refactor (use a var instead of property)
    var bcMessage = null;

    caseBase.broadcastMessage = function(msg){
      //caseBase.bcMessage = msg;
      bcMessage = msg;
      caseBase.initBroadcastMessage();
    };

    caseBase.initBroadcastMessage = function(message){
      $rootScope.$broadcast('httpRequestInitiated');      
    };

    return {
      getMessage: function(){
        //return caseBase.bcMessage;
        return bcMessage;
      },
      getSelected: function(caseId) {
        return backendService.get({
          specs: caseBase(caseId),
          onSuccess:function (resource, result) {
            result.push(new SfCase(resource.response.index));
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      getSelectedCommands: function(caseId) {
        return backendService.get({
          specs: caseBase(caseId),
          onSuccess:function (resource, result) {
            resource.response.commands.forEach(function(item){result.push(item)});
          }
        });
      },

      getPossibleResolutions: function(caseId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([
            {queries: 'possibleresolutions'}
            ]),
          onSuccess:function (resource, result) {
            resource.response.links.forEach(function(item){result.push(item)});
          }
        });
      },

      resolveCase: function(caseId, resolutionId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'resolve'}
            ]),
          {entity: resolutionId}).then(_.debounce(callback)());
      },

      getPossibleSendTo: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {queries: 'possiblesendto'}
          ]),
          onSuccess:function (resource, result) {
            resource.response.links.forEach(function(item){result.push(item)});
          }
        });
      },

      sendCaseTo: function(caseId, sendToId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'sendto'}
          ]),
          {entity: sendToId}).then(_.debounce(callback)()).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },

      closeCase: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'close'}
          ]),
          {}).then(callback).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },

      deleteCase: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'delete'}
          ]),
          {}).then(callback).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },

      assignCase: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'assign'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }, function(error){
            caseBase.broadcastMessage(error);
          }).then(callback);
      },

      unassignCase: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'unassign'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }, function(error){
            caseBase.broadcastMessage(error);
          }).then(callback);
      },

      markUnread: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'markunread'}
          ]),
          {}).then(callback).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },

      markRead: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'markread'}
          ]),
          {}).then(callback).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },

      Read: function(caseId) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'read'}
          ]),
          {}).then(callback).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },

      getSelectedNote: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{resources: 'note'}]),
          onSuccess:function (resource, result) {
            result.push(resource.response.index);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      addNote: function(caseId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'note'},
            {commands: 'addnote'}
          ]),
          value).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },

      getAllNotes: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'note'},
            {queries: 'allnotes'}
            ]),
          onSuccess:function (resource, result) {
            resource.response.links.forEach(function(item){result.push(item)});
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      getSelectedGeneral: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{resources: 'general'}]),
          onSuccess:function (resource, result) {
            var index = resource.response.index;

            if (index.dueOn)
              index.dueOnShort = index.dueOn.split("T")[0]

            result.push(index);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      getSelectedConversations: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{resources: 'conversations'}]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(link){
              result.push(link);
              caseBase.broadcastMessage(result.status);
            });
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      getSelectedAttachments: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{resources: 'attachments'}]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(link){
              result.push(link);
            });
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      deleteAttachment: function(caseId, attachmentId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'attachments'},
            {'index.links': attachmentId},
            {commands: 'delete'}
            ]),
          {}).then(_.debounce(callback)());
      },

      getSelectedContact: function(caseId, contactIndex) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'contacts', unsafe: true},
            {resources: contactIndex, unsafe: true}
          ]),
          onSuccess:function (resource, result) {
            result.push(resource.response.index);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      getSelectedContacts: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'contacts'}
          ]),
          onSuccess:function (resource, result) {
            resource.response.index.contacts.forEach(function(item){result.push(item)});
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      addContact: function(caseId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'contacts', unsafe: true},
            {commands: 'add'}
          ]),
          value).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },

      updateContact: function(caseId, contactIndex, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'contacts', unsafe: true},
            {resources: contactIndex, unsafe: true},
            {commands: 'update'}
          ]), 
          value).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },
      getCaseLogDefaultParams: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
              {resources: 'caselog'},
              {resources: 'defaultfilters', unsafe: true}
            ]),
          onSuccess:function (resource, result) {
            result.push(resource.response);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },
      getSelectedCaseLog: function(caseId) {
 
          //TODO: Look at why this is getting called twice on the caselog list page and if no way around it, maybe make sure the results are cached
          return backendService.get({
              specs:caseBase(caseId).concat([
                  {resources: 'caselog'},
                  {queries: 'list?system=true&systemTrace=true&form=true&conversation=true&attachment=true&contact=true&custom=true'}
              ]),
              onSuccess:function (resource, result) {
                  resource.response.links.forEach(function(link){
                      result.push(link);                      
                  });
                  caseBase.broadcastMessage(result.status);
              },
              onFailure:function(err){
                caseBase.broadcastMessage(err);      
              }
          });
      },
      getSelectedFilteredCaseLog: function(caseId, queryfilter) {
        //console.log(queryfilter);
        //TODO: Look at why this is getting called twice on the caslog list page and if no way around it, maybe make sure the results are cached
        return backendService.get({
          specs:caseBase(caseId).concat([
              {resources: 'caselog'},
              {queries: 'list?system='+ queryfilter.system +
              '&systemTrace='+ queryfilter.systemTrace +
              '&form='+ queryfilter.form +
              '&conversation='+ queryfilter.conversation +
              '&attachment='+ queryfilter.attachment +
              '&contact='+ queryfilter.contact +
              '&custom='+ queryfilter.custom +''}
            ]),
          onSuccess:function (resource, result) {
            resource.response.links.reverse().forEach(function(link){
              result.push(link);
            });
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },
      createCaseLogEntry: function(caseId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'caselog'},
            {commands: 'addmessage'}
          ]),
          {string: value}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },
      getPossibleCaseTypes: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'general'},
            {queries: 'possiblecasetypes'}
            ]),
          onSuccess:function (resource, result) {
            var caseTypeOptions = _.map(resource.response.links, function(link){
              return {name: link.text, value: link.id};
            });

            caseTypeOptions.forEach(function(item){result.push(item)});
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      updateSimpleValue: debounce(function(caseId, resource, command, property, value, callback) {

        var toSend = {};
        toSend[property] = value;

        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: resource},
            {commands: command}
          ]),
          toSend).then(_.debounce(callback)()).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      }, 1000),

      getSelectedPossibleForms: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{resources: 'possibleforms'}]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(item){result.push(item)});
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      getPossibleForm: function(caseId, formId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'possibleforms'},
            {'index.links': formId.replace("/", "")}
          ]),
          onSuccess:function (resource, result) {
            result.push(resource.response);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      createSelectedForm: function(caseId, formId) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'possibleforms'},
            {'index.links': formId.replace("/", "")},
            {commands: 'create'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },

      addViewModelProperties: function(pages){

        _.forEach(pages, function(page){
          _.forEach(page.fields, function(field){
            formMapper.addProperties(field)
          });
        });
      },

      getFormDraft: function(caseId, draftId) {
        var that = this;
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'formdrafts'},
            {'index.links': draftId}
            ]),
          onSuccess:function (resource, result) {
            var index = resource.response.index;

            index.draftId = draftId;

            index.enhancedPages = angular.copy(index.pages);
            that.addViewModelProperties(index.enhancedPages);

            result.push(index);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      getFormDraftFromForm: function(caseId, formId) {
        var that = this;
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'possibleforms'},
            {'index.links': formId.replace("/", "")},
            {queries: 'formdraft'}
            ]),
          onSuccess:function (resource, result) {
            var id = resource.response.id;

            return backendService.get({
            specs:caseBase(caseId).concat([
              {resources: 'formdrafts'},
              {resources: id, unsafe: true}
              ]),
            onSuccess:function (resource) {
              var index = resource.response.index;

              index.enhancedPages = angular.copy(index.pages);
              that.addViewModelProperties(index.enhancedPages);

              index.draftId = id;

              result.push(index);
              caseBase.broadcastMessage(result.status);
            },
            onFailure:function(err){
              caseBase.broadcastMessage(err);      
            }
          });
          //This might cause nestling issues with the error handler
          // to test, if the topmost error broadcaster overrides the nestled one
          caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      updateField: debounce(function(caseId, formId, fieldId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'formdrafts'},
            {'index.links': formId},
            {commands: 'updatefield'}
          ]),
          {field: fieldId, value: value}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      }, 1000),

      submitForm: function(caseId, formId) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'formdrafts'},
            {'index.links': formId},
            {commands: 'submit'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },

      getSubmittedForms: function(caseId, formId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{
            resources: 'submittedforms'
          }]),
          onSuccess:function (resource, result) {

            // NOTE: Need to index all forms and THEN filter them
            // since query takes `index` as parameter
            // where `index` is the index in the entire form list
            resource.response.index.forms.forEach(function(item, index){
              item.index = index;
            });

            var forms = _.filter(resource.response.index.forms, function(form){
              return form.id === formId;
            });

            forms.reverse().forEach(function(item, index){
              item.submissionDate = item.submissionDate.split("T")[0];
              result.push(item)
            });
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      getSubmittedForm: function(caseId, index) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'submittedforms'},
            {queries: 'submittedform?index=' + index}
          ]),
          onSuccess:function (resource, result) {
            result.push(resource.response);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },

      // Conversations
      createConversation: function(caseId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'conversations'},
            {commands: 'create'}
          ]),
          {topic: value}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },
      getConversationMessages: function(caseId, conversationId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'messages'}
            ]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(item){result.push(item)});
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },
      getMessageDraft: function(caseId, conversationId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'messages'},
            {resources: 'messagedraft', unsafe: true}
            ]),
          onSuccess:function (resource, result) {
            result.push(resource.response.index.string);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },
      updateMessageDraft: debounce(function(caseId, conversationId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'messages'},
            {resources: 'messagedraft', unsafe: true},
            {commands: 'changemessage'}
            ]),
          {message: value}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      }, 500),
      createMessage: function(caseId, conversationId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'messages'},
            {commands: 'createmessagefromdraft'}
            ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },
     getConversationParticipants: function(caseId, conversationId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'participants'}
            ]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(item){result.push(item)});
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },
      getPossibleConversationParticipants: function(caseId, conversationId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'participants'},
            {queries: 'possibleparticipants'}
            ]),
          onSuccess:function (resource, result) {
            resource.response.links.forEach(function(item){result.push(item)});
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);      
          }
        });
      },
      addParticipantToConversation: function(caseId, conversationId, participant) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'participants'},
            {commands: 'addparticipant'}
            ]),
          {entity: participant}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },
      deleteParticipantFromConversation: function(caseId, conversationId, participant) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'participants'},
            {'index.links': participant},
            {commands: 'delete'}
            ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      }

    }
  }]);

  // https://github.com/angular/angular.js/issues/2690
  sfServices.factory('debounce', ['$timeout', function ($timeout) {
      return function(fn, timeout, apply){ // debounce fn
          timeout = angular.isUndefined(timeout) ? 0 : timeout;
          apply = angular.isUndefined(apply) ? true : apply; // !!default is true! most suitable to my experience
          var nthCall = 0;
          return function(){ // intercepting fn
              var that = this;
              var argz = arguments;
              nthCall++;
              var later = (function(version){
                  return function(){
                      if (version === nthCall){
                          return fn.apply(that, argz);
                      }
                  };
              })(nthCall);
              return $timeout(later, timeout, apply);
          };
      };
  }]);

})();
