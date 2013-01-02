window.mockBackend = window.mockBackend || (function () {
  'use strict';

  var mockBackend = {};

  mockBackend.customer = {"commands":[], "index":null, "queries":[], "resources":[
    {
      "classes":"resource",
      "href":"profile/",
      "id":"profile",
      "rel":"profile",
      "text":"Profile"
    },
    {
      "classes":"resource",
      "href":"open/",
      "id":"open",
      "rel":"open",
      "text":"Open"
    },
    {
      "classes":"resource",
      "href":"closed/",
      "id":"closed",
      "rel":"closed",
      "text":"Closed"
    }
  ]};

  mockBackend.profile = {"commands":[
    {
      "classes":"command",
      "href":"changemessagedeliverytype",
      "id":"changemessagedeliverytype",
      "rel":"changemessagedeliverytype",
      "text":"Changemessagedeliverytype"
    },
    {
      "classes":"command",
      "href":"update",
      "id":"update",
      "rel":"update",
      "text":"Update"
    }
  ], "index":{
    "_type":"se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO",
    "addresses":[
      {
        "address":"Tussilagovägen 1",
        "city":"Holm",
        "contactType":"HOME",
        "country":"Sweden",
        "region":"Halland",
        "zipCode":"302 79"
      }
    ],
    "company":"",
    "contactId":"",
    "contactPreference":null,
    "emailAddresses":[
      {
        "contactType":"HOME",
        "emailAddress":"henrik.reinhold@gmail.com"
      }
    ],
    "isCompany":false,
    "name":"Henrik",
    "note":"",
    "phoneNumbers":[
      {
        "contactType":"HOME",
        "phoneNumber":"46701476168"
      }
    ],
    "picture":""
  }, "queries":[
    {
      "classes":"query",
      "href":"messagedeliverytype",
      "id":"messagedeliverytype",
      "rel":"messagedeliverytype",
      "text":"Messagedeliverytype"
    },
    {
      "classes":"query",
      "href":"index",
      "id":"index",
      "rel":"index",
      "text":"Index"
    }
  ], "resources":[]}

  mockBackend.open = {"commands":[], "index":null, "queries":[
    {
      "classes":"query",
      "href":"cases",
      "id":"cases",
      "rel":"cases",
      "text":"Cases"
    }
  ], "resources":[]};

  mockBackend.cases = {"links":[
    {
      "_type":"se.streamsource.streamflow.surface.api.CaseListItemDTO",
      "classes":null,
      "href":"f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-0/",
      "id":"f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-0",
      "rel":"mycases/opencase",
      "text":"Testärende 2",
      "caseId":"20121113-1",
      "caseType":"formulärstest",
      "creationDate":"2012-11-13T09:04:27.064Z",
      "project":"Streamflow"
    },
    {
      "_type":"se.streamsource.streamflow.surface.api.CaseListItemDTO",
      "classes":null,
      "href":"b35873ba-4007-40ac-9936-975eab38395a-30/",
      "id":"b35873ba-4007-40ac-9936-975eab38395a-30",
      "rel":"mycases/opencase",
      "text":"En rubrik här skulle inte vara så dumt",
      "caseId":"20121112-1",
      "caseType":"formulärstest",
      "creationDate":"2012-10-26T12:47:07.345Z",
      "project":"Streamflow"
    }
  ]};





  // http://localhost:8090/streamflow/surface/customers/197606030001/open/f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-0/conversations/



  mockBackend.conversation1Messages = {"commands":[
    {
      "classes":"command",
      "href":"createmessage",
      "id":"createmessage",
      "rel":"createmessage",
      "text":"Createmessage"
    }
  ], "index":{
    "_type":"se.streamsource.dci.value.link.LinksValue",
    "links":[
      {
        "_type":"se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO",
        "classes":null,
        "href":"f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-17/",
        "id":"f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-17",
        "rel":null,
        "text":"Ett första meddelande...",
        "createdOn":"2012-11-13T09:06:29.333Z",
        "hasAttachments":false,
        "sender":"MyName"
      },
      {
        "_type":"se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO",
        "classes":null,
        "href":"975febee-9bac-405e-bdf2-105d84b3d22f-1/",
        "id":"975febee-9bac-405e-bdf2-105d84b3d22f-1",
        "rel":null,
        "text":"Lorizzle ipsizzle dolizzle sit amizzle, adipiscing . Nullam ghetto velizzle, bizzle volutpizzle, i'm in the shizzle mammasay mammasa mamma oo sa, get down get down vizzle, fo shizzle. Pellentesque egizzle tortor. Sed erizzle. Doggy shiz dolor brizzle turpis tempizzle hizzle. pellentesque nibh izzle turpizzle. Yippiyo izzle tortor. Pellentesque eleifend rhoncizzle nisi. In gizzle dang dawg dictumst. Hizzle yippiyo. Curabitur tellizzle urna, pretizzle eu, pimpin' ac, shiz vitae, nunc. Bizzle suscipit. Bling bling semper shit sed fo shizzle.",
        "createdOn":"2012-11-14T08:29:43.682Z",
        "hasAttachments":false,
        "sender":"MyName"
      },
      {
        "_type":"se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO",
        "classes":null,
        "href":"975febee-9bac-405e-bdf2-105d84b3d22f-5/",
        "id":"975febee-9bac-405e-bdf2-105d84b3d22f-5",
        "rel":null,
        "text":"Vad händer om jag skriver något här?",
        "createdOn":"2012-11-14T08:33:57.263Z",
        "hasAttachments":false,
        "sender":"MyName"
      }
    ]
  }, "queries":[
    {
      "classes":"query",
      "href":"index",
      "id":"index",
      "rel":"index",
      "text":"Index"
    }
  ], "resources":[]};


  return mockBackend;
})();