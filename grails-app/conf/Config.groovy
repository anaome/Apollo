// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

extraSrcDirs = "$basedir/src/gwt/org.bbop.apollo.gwt.shared"
eventCompileStart = {
    projectCompiler.srcDirectories << extraSrcDirs
}

grails.config.locations = [
        "file:./${appName}-config.groovy"        // dev only
        , "classpath:${appName}-config.groovy"    // for production deployment
        , "classpath:${appName}-config.properties"
]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }


grails.assets.minifyJs = false
grails.assets.minifyCss = false
grails.assets.enableSourceMaps = true
grails.assets.bundle = false
grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination

// The ACCEPT header will not be used for content negotiation for user agents containing the following strings (defaults to the 4 major rendering engines)
grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
grails.mime.types = [ // the first one is the default format
                      all          : '*/*', // 'all' maps to '*' or the first available format in withFormat
                      atom         : 'application/atom+xml',
                      css          : 'text/css',
                      csv          : 'text/csv',
                      form         : 'application/x-www-form-urlencoded',
                      html         : ['text/html', 'application/xhtml+xml'],
                      js           : 'text/javascript',
                      json         : ['application/json', 'text/json'],
                      multipartForm: 'multipart/form-data',
                      rss          : 'application/rss+xml',
                      text         : 'text/plain',
                      hal          : ['application/hal+json', 'application/hal+xml'],
                      xml          : ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// Legacy setting for codec used to encode data with ${}
grails.views.default.codec = "html"

// The default scope for controllers. May be prototype, session or singleton.
// If unspecified, controllers are prototype scoped.
grails.controllers.defaultScope = 'singleton'

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside ${}
                scriptlet = 'html' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        // filteringCodecForContentType.'text/html' = 'html'
    }
}


grails.converters.encoding = "UTF-8"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

// configure passing transaction's read-only attribute to Hibernate session, queries and criterias
// set "singleSession = false" OSIV mode in hibernate configuration after enabling
grails.hibernate.pass.readonly = false
// configure passing read-only to OSIV session by default, requires "singleSession = false" OSIV mode
grails.hibernate.osiv.readonly = false


environments {
    development {
        grails.logging.jul.usebridge = true
    }
    production {
        grails.logging.jul.usebridge = false
        // TODO: grails.serverURL = "http://www.changeme.com"
    }
}

// log4j configuration
log4j.main = {
    // Example of changing the log pattern for the default console appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error 'org.codehaus.groovy.grails.web.servlet',        // controllers
            'org.codehaus.groovy.grails.web.pages',          // GSP
            'org.codehaus.groovy.grails.web.sitemesh',       // layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping',        // URL mapping
            'org.codehaus.groovy.grails.commons',            // core / classloading
            'org.codehaus.groovy.grails.plugins',            // plugins
            'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'

//    trace 'org.hibernate.type'
//    debug 'org.hibernate.SQL'

    warn 'grails.app'
//    debug 'grails.app'

    //trace 'org.hibernate.type'
    //debug 'org.hibernate.SQL'
    //debug 'grails.app'
    //debug 'grails.app.controllers.org.bbop.apollo'
    //debug 'grails.app.controllers.org.bbop.apollo.JbrowseController'
    //info  'grails.app.services'
    //debug 'grails.app.controllers.edu.uoregon.nic.nemo.portal'
    //debug 'grails.app.jobs'
    //debug 'grails.app.taglib'
    //debug 'grails.app.taglib.edu.uoregon.nic.nemo.portal'
    //debug 'grails.app.controllers'
    //debug 'grails.app.services'
    //debug 'grails.app.services.edu.uoregon.nic.nemo.portal.OntologyService'
    //debug 'grails.app.services.edu.uoregon.nic.nemo.portal.DataStubService'
    //debug 'grails.app.services.edu.uoregon.nic.nemo.portal.UserService'
    //debug 'grails.app.controllers.edu.uoregon.nic.nemo.portal'
    //debug 'grails.app.controllers.edu.uoregon.nic.nemo.portal.TermController'
}

//grails.gorm.default.constraints = {
//    '*'(nullable: true)
//}
//grails.datastore.gorm.GormInstanceApi.copy = cloneForDomains ;
grails.gorm.failOnError = true
// https://github.com/zyro23/grails-spring-websocket
// websocket info
grails.tomcat.nio = true
grails.tomcat.scan.enabled = true

// default apollo settings
apollo {
    default_minimum_intron_size = 1
    history_size = 0
    overlapper_class = "org.bbop.apollo.sequence.OrfOverlapper"
    track_name_comparator = "/config/track_name_comparator.js"
    use_cds_for_new_transcripts = true
    feature_has_dbxrefs = true
    feature_has_attributes = true
    feature_has_pubmed_ids = true
    feature_has_go_ids = true
    feature_has_comments = true
    feature_has_status = true
    user_pure_memory_store = true
    translation_table = "/config/translation_tables/ncbi_1_translation_table.txt"
    is_partial_translation_allowed = false // unused so far
    get_translation_code = 1
    sequence_search_tools {
        blat_nuc {
            search_exe = "/usr/local/bin/blat"
            search_class = "org.bbop.apollo.sequence.search.blat.BlatCommandLineNucleotideToNucleotide"
            name = "Blat nucleotide"
            params = ""
        }
        blat_prot {
            search_exe = "/usr/local/bin/blat"
            search_class = "org.bbop.apollo.sequence.search.blat.BlatCommandLineProteinToNucleotide"
            name = "Blat protein"
            params = ""
            //tmp_dir = "/opt/apollo/tmp"
        }
    }

    // TODO: should come from config or via preferences database
    splice_donor_sites = ["GT"]
    splice_acceptor_sites = ["AG"]
    gff3.source= "."
    bootstrap = false

    info_editor = {
        feature_types = "default"
        attributes = true
        dbxrefs = true
        pubmed_ids = true
        go_ids = true
        comments = true
    }

    administrativePanel = [
            ['label': "Canned Comments", 'link': "/cannedComment/"]
            ,['label': "Feature Types", 'link': "/featureType/"]
            ,['label': "Statuses", 'link': "/availableStatus/"]
//            , ['label': "Other Admin", 'link': "/annotator/cannedComments/"]
    ]

    customPanel = [
//            ['name':'GenSas2','link':'http://localhost/gensas2']
    ]

    // comment out if you don't want this to be reported
    google_analytics = "UA-62921593-1"

}

// from: http://grails.org/plugin/audit-logging
// may end up going away
auditLog {
    //note, this disables the audit log
//    disabled = true
    //verbose = true // verbosely log all changed values to db
    logIds = true  // log db-ids of associated objects.

}
