class UrlMappings {
    static mappings = {
        "/" {
            controller = "bucket"
                action = "index"
        }
        "/$controller/$action?/$id?"{
            constraints {
                // apply constraints here
            }
    }
    "500"(view:'/error')
    }
}
