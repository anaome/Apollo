## WebApollo Configuration

View <a href="https://github.com/GMOD/Apollo/blob/master/docs/Configure.md">On GitHub</a>

Web Apollo 2.0 includes some basic configuration parameters that are specified in configuration files. The most
important parameters are the database parameters in order to get Web Apollo up and running. Other options besides the
database parameters can be configured via the config files, but note that many parameters can also be configured via the
web interface.

Note: Configuration options may change over time, as more configuration items are integrated into the web interface.


### Database configuration


You can choose from using H2, Postgres, or MySQL database configurations by default.

Each has a file called `sample-h2-apollo-config.groovy` or `sample-postgres-apollo-config.groovy` that is designed to be
renamed to apollo-config.groovy before running `apollo deploy`.

The database configurations in `apollo-config.groovy` can be used in test, development, and production modes, and the
environment will be automatically selected depending on how it is run, e.g:

* `apollo deploy` or `apollo release` for a production environment
* `apollo run-local` or `apollo debug` for a development environment
* `apollo test` for a test environment

*Note:* additional general configuration from the "Main configuration" can also be added to your apollo-config.groovy.

*Note:* to deploy on tomcat you *NEED* to have a configured `apollo-config` file copied from one of the samples.

### Main configuration

The main configuration settings for Apollo are stored in Config.groovy, but you can override settings in your
`apollo-config.groovy` file (i.e. the same file that contains your database parameters). Here are the defaults that are
defined in the Config.groovy file:


    // default apollo settings
    apollo {
        default_minimum_intron_size = 1
        history_size = 0
        overlapper_class = "org.bbop.apollo.sequence.OrfOverlapper"
        track_name_comparator = "/config/track_name_comparator.js"
        use_cds_for_new_transcripts = true
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
            }
        }

        // TODO: should come from config or via preferences database
        splice_donor_sites = [ "GT"]
        splice_acceptor_sites = [ "AG"]
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
    }

These settings are essentially the same familiar parameters from a config.xml file from previous Web Apollo versions.
The defaults are generally sufficient, but as noted above, you can override any particular parameter in your
apollo-config.groovy file, e.g. you can add override configuration any given parameter as follows:

    grails {
        apollo.get_translation_code = 1 
        apollo {
             use_cds_for_new_transcripts = true
             default_minimum_intron_size = 1
             get_translation_code = 1  // identical to the dot notation
        }
    }

 

### Canned comments

Todo


### Search tools

As shown in the Main configuration, Web Apollo allows the user to specify sequence search tools. The tool UCSC BLAT is
commonly used and can be easily configured via the config file, with the general parameters given as follows:

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
            // tmp_dir = /custom/tmp/dir
        }
    }


Note: Any arbitrary search tool can be specified using this syntax, i.e. the blat_nuc and blat_prot are just recommended
defaults. You could have your own section with multiple different parameters, or even code your own search_class if it
implements the SequenceSearchTool interface. Also note: the tmp_dir is normally a system predefined folder, so it is not
normally necessary to define it.

### Supported annotation types

Many configurations will require you to define which annotation types the configuration will apply to. Web Apollo
supports the following "higher level" types (from the Sequence Ontology):

* sequence:gene
* sequence:pseudogene
* sequence:transcript
* sequence:mRNA
* sequence:tRNA
* sequence:snRNA
* sequence:snoRNA
* sequence:ncRNA
* sequence:rRNA
* sequence:miRNA
* sequence:repeat_region
* sequence:transposable_element


### Apache / Nginx Configuration

Often time admins will put Apache or Nginx in front of a servlet container (e.g., Tomcat, Jetty) and proxy calls.  
This is not necessary, but it is a very standard configuration.  

One change (from previous verions) is support for web sockets.  We use the SockJS library, which will downgrade to 
long-polling if web sockets are not available. 

To avoid this (performance may be affected and firewalls perfer web sockets):
- every modern web browsers support web sockets  (http://caniuse.com/#feat=websockets)
- Jetty, and Tomcat (7 and 8) both support web sockets unless VERY old so everytyhing should just work.

Assuming that Tomcat / Jetty is serving from the same host on the standard port 8080.

#### Apache Proxy 

There are many ways to do proxy apache to tomcat.   Using proxy-html.conf (make sure its activated and installed) add lines similar to these.
To proxy websockets explicity you need mod_proxy_wstunnel (typically built in):  http://httpd.apache.org/docs/2.4/mod/mod_proxy_wstunnel.html

First uncomment it (on a mac) or use a2enmod enable it (on ubuntu / debian):

    LoadModule proxy_wstunnel_module libexec/apache2/mod_proxy_wstunnel.so


In http.conf or proxy-html.conf add the lines:

    ProxyRequests     off
    ProxyPreserveHost on
    
    ProxyPass "/apollo/stomp"  "ws://localhost:8080/apollo/stomp"
    ProxyPassReverse "/apollo/stomp" "ws://localhost:8080/apollo/stomp"
    
    <Proxy *>
         Order deny,allow
         Allow from all
    </Proxy>
    
    
    
    ProxyPass  /apollo http://localhost:8080/apollo
    ProxyPassReverse  /apollo http://localhost:8080/apollo
    ProxyPassReverseCookiePath  / http://localhost:8080/apollo
    


#### Nginx Proxy (from version 1.4 on)

Your setup may vary, but setting the upgrade headers is the key part and .

    map $http_upgrade $connection_upgrade {
            default upgrade;
            ''      close;
    }
    
    
    server {
        ## Main
        listen   80;
        server_name  myserver;
        
        # . . . other settings
        location /ApolloSever {
            # . . . other settings
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection $connection_upgrade;
            proxy_pass      http://127.0.0.1:8080;
        }
    }


### Data adapters

#### GFF3

Todo

#### FASTA
Todo

### Upgrading existing instances

Todo


#### Upgrading existing JBrowse data stores

It is not necessary to upgrade the JBrowse data tracks to use Web Apollo 2.0, you can just point to the data directory
from your previous instances from the Organism panel.

##### Sequence alterations updating

Todo
