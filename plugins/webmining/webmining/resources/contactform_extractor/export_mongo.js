conn = new Mongo();
db = conn.getDB("companies");

var docs = db.company.find({"companyCommunication.contactForms": {$exists: true}});

while(docs.hasNext()) {
    c = docs.next();
    if (c.companyCommunication.contactForms.length > 0) {
        for(var i=0; i < c.companyCommunication.contactForms.length; i++) {
            print(c.officialName + '\t' + c.companyCommunication.contactForms[i]);
        }
    }
}

