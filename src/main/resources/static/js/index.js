(function($){
    $(document).ready(() => {
         $('#calls').DataTable({
            searching: false,
            deferRender: true,
            ajax: function(_data, callback) {
                $.ajax({
                    url: "/actuator/httptrace",
                    success: function(data){
                        callback({
                            data: data.traces.map((d) => ({
                                date: d.timestamp,
                                uri: d.request.uri,
                                status: d.response.status,
                                agent: d.request.headers["user-agent"],
                                duration: d.timeTaken
                            }))
                        });
                    }
                })
            },
            columns: [
                 {
                    data: "date",
                    render: (data) => {
                        return new Date(data).toLocaleString()
                    }
                 },
                 { data: "uri" },
                 { data: "status" },
                 { data: "agent" },
                 { data: "duration" }
            ],
            columnDefs: [
                 { "type": "date", "targets": 0 },
                 { "type": "num",  "targets": 2 },
                 { "type": "num",  "targets": 4 }
            ],
            order: [[ 0, "desc" ]]
         });
    })
})(jQuery)