package com.ctel.dbaas.init_data;

import com.ctel.dbaas.common.enums.DataTypeConfig;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class FactoryConfigDatastore {

    public static List<ConfigDatastore> loadConfig(String datastoreNameVersion) {
        List<ConfigDatastore> config = new ArrayList<>();
        switch (datastoreNameVersion) {
            case "postgresql:14.9:standalone", "postgresql:14.9:master_slave", "postgresql:14.9:cluster_ha" -> {
                config.add(new ConfigDatastore("authentication_timeout",
                        "60",
                        "1-600",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum allowed time to complete client authentication",
                        "seconds", false));
                config.add(new ConfigDatastore(
                        "autovacuum_analyze_scale_factor",
                        "0.1", "0-100",
                        DataTypeConfig.Double.name(),
                        "Number of tuple inserts, updates, or deletes prior to analyze as a fraction of reltuples",
                        "", false));
                config.add(new ConfigDatastore(
                        "autovacuum_analyze_threshold",
                        "50", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Minimum number of tuple inserts, updates, or deletes prior to analyze",
                        "", false));
                config.add(new ConfigDatastore(
                        "autovacuum_freeze_max_age",
                        "200000000", "100000-2000000000",
                        DataTypeConfig.Integer.name(),
                        "Age at which to autovacuum a table to prevent transaction ID wraparound",
                        "", true));
                config.add(new ConfigDatastore(
                        "autovacuum_max_workers",
                        "3", "1-262143",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum number of simultaneously running autovacuum worker processes",
                        "", true));
                config.add(new ConfigDatastore(
                        "autovacuum_multixact_freeze_max_age",
                        "400000000", "10000-2000000000",
                        DataTypeConfig.Integer.name(),
                        "Multixact age at which to autovacuum a table to prevent multixact wraparound",
                        "", true));
                config.add(new ConfigDatastore(
                        "autovacuum_naptime",
                        "60", "1-2147483",
                        DataTypeConfig.Integer.name(),
                        "Time to sleep between autovacuum runs",
                        "seconds", false));
                config.add(new ConfigDatastore(
                        "autovacuum_vacuum_cost_delay",
                        "2", "-1-100",
                        DataTypeConfig.Double.name(),
                        "Vacuum cost delay in milliseconds, for autovacuum",
                        "milliseconds", false));
                config.add(new ConfigDatastore(
                        "autovacuum_vacuum_cost_limit",
                        "-1", "-1-10000",
                        DataTypeConfig.Integer.name(),
                        "Vacuum cost amount available before napping, for autovacuum",
                        "", false));
                config.add(new ConfigDatastore(
                        "autovacuum_vacuum_scale_factor",
                        "0.2", "0-100",
                        DataTypeConfig.Double.name(),
                        "Number of tuple updates or deletes prior to vacuum as a fraction of reltuples",
                        "", false));
                config.add(new ConfigDatastore(
                        "autovacuum_vacuum_threshold",
                        "50", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Minimum number of tuple updates or deletes prior to vacuum",
                        "", false));
                config.add(new ConfigDatastore(
                        "autovacuum_work_mem",
                        "-1", "-1-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum memory to be used by each autovacuum worker process",
                        "KB", false));
                config.add(new ConfigDatastore(
                        "backend_flush_after",
                        "0", "0-256",
                        DataTypeConfig.Integer.name(),
                        "Number of pages after which previously performed writes are flushed to disk",
                        "KB", false));
                config.add(new ConfigDatastore(
                        "backslash_quote",
                        "safe_encoding", "safe_encoding,on,off",
                        DataTypeConfig.Enum.name(),
                        "Sets whether \"\\'\" is allowed in string literals",
                        "", false));
                config.add(new ConfigDatastore(
                        "bgwriter_delay",
                        "200", "10-10000",
                        DataTypeConfig.Integer.name(),
                        "Background writer sleep time between rounds",
                        "milliseconds", false));
                config.add(new ConfigDatastore(
                        "bgwriter_flush_after",
                        "64", "0-256",
                        DataTypeConfig.Integer.name(),
                        "Number of pages after which previously performed writes are flushed to disk",
                        "8kB", false));
                config.add(new ConfigDatastore(
                        "bgwriter_lru_maxpages",
                        "100", "0-1073741823",
                        DataTypeConfig.Integer.name(),
                        "Background writer maximum number of LRU pages to flush per round",
                        "", false));
                config.add(new ConfigDatastore(
                        "bgwriter_lru_multiplier",
                        "2", "0-10",
                        DataTypeConfig.Double.name(),
                        "Multiple of the average buffer usage to free per round",
                        "", false));
                config.add(new ConfigDatastore(
                        "bonjour_name",
                        "", "",
                        DataTypeConfig.String.name(),
                        "Sets the Bonjour service name",
                        "", true));
                config.add(new ConfigDatastore(
                        "bytea_output",
                        "hex", "escape,hex",
                        DataTypeConfig.Enum.name(),
                        "Sets the output format for bytea",
                        "", false));
                config.add(new ConfigDatastore(
                        "checkpoint_completion_target",
                        "0.9", "0-1",
                        DataTypeConfig.Double.name(),
                        "Time spent flushing dirty buffers during checkpoint, as fraction of checkpoint interval",
                        "", false));
                config.add(new ConfigDatastore(
                        "checkpoint_flush_after",
                        "32", "0-256",
                        DataTypeConfig.Integer.name(),
                        "Number of pages after which previously performed writes are flushed to disk",
                        "8kB", false));
                config.add(new ConfigDatastore(
                        "checkpoint_timeout",
                        "300", "30-86400",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum time between automatic WAL checkpoints",
                        "seconds", false));
                config.add(new ConfigDatastore(
                        "checkpoint_warning",
                        "30", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Enables warnings if checkpoint segments are filled more frequently than this",
                        "seconds", false));
                config.add(new ConfigDatastore(
                        "client_encoding",
                        "SQL_ASCII", "",
                        DataTypeConfig.String.name(),
                        "Sets the client's character set encoding",
                        "", false));
                config.add(new ConfigDatastore(
                        "client_min_messages",
                        "notice", "debug5,debug4,debug3,debug2,debug1,log,notice,warning,error",
                        DataTypeConfig.Enum.name(),
                        "Sets the message levels that are sent to the client",
                        "", false));
                config.add(new ConfigDatastore(
                        "commit_delay",
                        "0", "0-100000",
                        DataTypeConfig.Integer.name(),
                        "Sets the delay in microseconds between transaction commit and flushing WAL to disk",
                        "", false));
                config.add(new ConfigDatastore(
                        "commit_siblings",
                        "5", "0-1000",
                        DataTypeConfig.Integer.name(),
                        "Sets the minimum concurrent open transactions before performing commit_delay",
                        "", false));
                config.add(new ConfigDatastore(
                        "constraint_exclusion",
                        "partition", "partition,on,off",
                        DataTypeConfig.Enum.name(),
                        "Enables the planner to use constraints to optimize queries",
                        "", false));
                config.add(new ConfigDatastore(
                        "cpu_index_tuple_cost",
                        "0.005", "0-1.79769e+308",
                        DataTypeConfig.Double.name(),
                        "Sets the planner's estimate of the cost of processing each index entry during an index scan",
                        "", false));
                config.add(new ConfigDatastore(
                        "cpu_operator_cost",
                        "0.0025", "0-1.79769e+308",
                        DataTypeConfig.Double.name(),
                        "Sets the planner's estimate of the cost of processing each operator or function call",
                        "", false));
                config.add(new ConfigDatastore(
                        "cpu_tuple_cost",
                        "0.01", "0-1.79769e+308",
                        DataTypeConfig.Double.name(),
                        "Sets the planner's estimate of the cost of processing each tuple (row)",
                        "", false));
                config.add(new ConfigDatastore(
                        "cursor_tuple_fraction",
                        "0.1", "0-1",
                        DataTypeConfig.Double.name(),
                        "Sets the planner's estimate of the fraction of a cursor's rows that will be retrieved",
                        "", false));
                config.add(new ConfigDatastore(
                        "DateStyle",
                        "ISO, MDY", "",
                        DataTypeConfig.String.name(),
                        "Sets the display format for date and time values",
                        "", false));
                config.add(new ConfigDatastore(
                        "deadlock_timeout",
                        "1000", "1-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the time to wait on a lock before checking for deadlock",
                        "milliseconds", false));
                config.add(new ConfigDatastore(
                        "default_statistics_target",
                        "100", "1-10000",
                        DataTypeConfig.Integer.name(),
                        "Sets the default statistics target",
                        "", false));
                config.add(new ConfigDatastore(
                        "default_tablespace",
                        "", "",
                        DataTypeConfig.String.name(),
                        "Sets the default tablespace to create tables and indexes in",
                        "", false));
                config.add(new ConfigDatastore(
                        "default_text_search_config",
                        "pg_catalog.simple", "",
                        DataTypeConfig.String.name(),
                        "Sets default text search configuration",
                        "", false));
                config.add(new ConfigDatastore(
                        "default_transaction_isolation",
                        "read committed", "serializable,repeatable read,read committed,read uncommitted",
                        DataTypeConfig.Enum.name(),
                        "Sets the transaction isolation level of each new transaction",
                        "", false));
                config.add(new ConfigDatastore(
                        "dynamic_shared_memory_type",
                        "posix", "posix,sysv,mmap",
                        DataTypeConfig.Enum.name(),
                        "Selects the dynamic shared memory implementation used",
                        "", false));
                config.add(new ConfigDatastore(
                        "effective_cache_size",
                        "524288", "1-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the planner's assumption about the total size of the data caches",
                        "8kB", false));
                config.add(new ConfigDatastore(
                        "effective_io_concurrency",
                        "1", "0-1000",
                        DataTypeConfig.Integer.name(),
                        "Number of simultaneous requests that can be handled efficiently by the disk subsystem",
                        "", false));
                config.add(new ConfigDatastore(
                        "extra_float_digits",
                        "1", "-15-3",
                        DataTypeConfig.Integer.name(),
                        "Sets the number of digits displayed for floating-point values",
                        "", false));
                config.add(new ConfigDatastore(
                        "from_collapse_limit",
                        "8", "1-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the FROM-list size beyond which subqueries are not collapsed",
                        "", false));
                config.add(new ConfigDatastore(
                        "geqo_effort",
                        "5", "1-10",
                        DataTypeConfig.Integer.name(),
                        "GEQO: effort is used to set the default for other GEQO parameters",
                        "", false));
                config.add(new ConfigDatastore(
                        "geqo_generations",
                        "0", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "GEQO: number of iterations of the algorithm",
                        "", false));
                config.add(new ConfigDatastore(
                        "geqo_pool_size",
                        "0", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "GEQO: number of individuals in the population",
                        "", false));
                config.add(new ConfigDatastore(
                        "geqo_seed",
                        "0", "0-1",
                        DataTypeConfig.Double.name(),
                        "GEQO: seed for random path selection",
                        "", false));
                config.add(new ConfigDatastore(
                        "geqo_selection_bias",
                        "2", "1.5-2",
                        DataTypeConfig.Double.name(),
                        "GEQO: selective pressure within the population",
                        "", false));
                config.add(new ConfigDatastore(
                        "geqo_threshold",
                        "12", "2-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the threshold of FROM items beyond which GEQO is used",
                        "", false));
                config.add(new ConfigDatastore(
                        "gin_pending_list_limit",
                        "4096", "64-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum size of the pending list for GIN index",
                        "KB", false));
                config.add(new ConfigDatastore(
                        "huge_pages",
                        "try", "off,on,try",
                        DataTypeConfig.Enum.name(),
                        "Use of huge pages on Linux or Windows",
                        "", false));
                config.add(new ConfigDatastore(
                        "idle_in_transaction_session_timeout",
                        "0", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum allowed idle time between queries, when in a transaction",
                        "milliseconds", false));
                config.add(new ConfigDatastore(
                        "IntervalStyle",
                        "postgres", "postgres,postgres_verbose,sql_standard,iso_8601",
                        DataTypeConfig.Enum.name(),
                        "Sets the display format for interval values",
                        "", false));
                config.add(new ConfigDatastore(
                        "join_collapse_limit",
                        "8", "1-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the FROM-list size beyond which JOIN constructs are not flattened",
                        "", false));
                config.add(new ConfigDatastore(
                        "lc_messages",
                        "", "",
                        DataTypeConfig.String.name(),
                        "Sets the language in which messages are displayed",
                        "", false));
                config.add(new ConfigDatastore(
                        "lc_monetary",
                        "C", "",
                        DataTypeConfig.String.name(),
                        "Sets the locale for formatting monetary amounts",
                        "", false));
                config.add(new ConfigDatastore(
                        "lc_numeric",
                        "C", "",
                        DataTypeConfig.String.name(),
                        "Sets the locale for formatting numbers",
                        "", false));
                config.add(new ConfigDatastore(
                        "lc_time",
                        "C", "",
                        DataTypeConfig.String.name(),
                        "Sets the locale for formatting date and time values",
                        "", false));
                config.add(new ConfigDatastore(
                        "lock_timeout",
                        "0", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum allowed duration of any wait for a lock",
                        "milliseconds", false));
                config.add(new ConfigDatastore(
                        "log_autovacuum_min_duration",
                        "-1", "-1-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the minimum execution time above which autovacuum actions will be logged",
                        "milliseconds", false));
                config.add(new ConfigDatastore(
                        "log_error_verbosity",
                        "default", "terse,default,verbose",
                        DataTypeConfig.Enum.name(),
                        "Sets the verbosity of logged messages",
                        "", false));

                config.add(new ConfigDatastore(
                        "xmloption",
                        "content", "content,document",
                        DataTypeConfig.Enum.name(),
                        "Sets whether XML data in implicit parsing and serialization operations is to be considered as documents or content fragments",
                        "", false));
                config.add(new ConfigDatastore(
                        "xmlbinary",
                        "base64", "base64,hex",
                        DataTypeConfig.Enum.name(),
                        "Sets how binary values are to be encoded in XML",
                        "", false));
                config.add(new ConfigDatastore(
                        "work_mem",
                        "4096", "64-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum memory to be used for query workspaces",
                        "KB", false));
                config.add(new ConfigDatastore(
                        "wal_writer_delay",
                        "200", "1-1000",
                        DataTypeConfig.Integer.name(),
                        "Time between WAL flushes performed in the WAL writer",
                        "ms", false));
                config.add(new ConfigDatastore(
                        "wal_sync_method",
                        "fdatasync", "fsync,fdatasync,open_sync,open_datasync",
                        DataTypeConfig.Enum.name(),
                        "Selects the method used for forcing WAL updates to disk",
                        "", false));
                config.add(new ConfigDatastore(
                        "wal_receiver_status_interval",
                        "10", "0-2147483",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum interval between WAL receiver status reports to the sending server",
                        "s", false));
                config.add(new ConfigDatastore(
                        "wal_receiver_timeout",
                        "60000", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum wait time to receive data from the sending server",
                        "ms", false));
                config.add(new ConfigDatastore(
                        "wal_sender_timeout",
                        "60000", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum time to wait for WAL replication",
                        "ms", false));
                config.add(new ConfigDatastore(
                        "wal_keep_size",
                        "0", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the size of WAL files held for standby servers",
                        "MB", false));
                config.add(new ConfigDatastore(
                        "wal_buffers",
                        "-1", "-1-262143",
                        DataTypeConfig.Integer.name(),
                        "Sets the number of disk-page buffers in shared memory for WAL",
                        "8kB", false));
                config.add(new ConfigDatastore(
                        "vacuum_multixact_freeze_table_age",
                        "150000000", "0-2000000000",
                        DataTypeConfig.Integer.name(),
                        "Multixact age at which VACUUM should scan whole table to freeze tuples",
                        "", false));
                config.add(new ConfigDatastore(
                        "vacuum_multixact_freeze_min_age",
                        "5000000", "0-1000000000",
                        DataTypeConfig.Integer.name(),
                        "Minimum age at which VACUUM should freeze a MultiXactId in a table row",
                        "", false));
                config.add(new ConfigDatastore(
                        "vacuum_freeze_table_age",
                        "150000000", "0-2000000000",
                        DataTypeConfig.Integer.name(),
                        "Age at which VACUUM should scan whole table to freeze tuples",
                        "", false));
                config.add(new ConfigDatastore(
                        "vacuum_freeze_min_age",
                        "50000000", "0-1000000000",
                        DataTypeConfig.Integer.name(),
                        "Minimum age at which VACUUM should freeze a table row",
                        "", false));
                config.add(new ConfigDatastore(
                        "vacuum_defer_cleanup_age",
                        "0", "0-1000000",
                        DataTypeConfig.Integer.name(),
                        "Number of transactions by which VACUUM and HOT cleanup should be deferred, if any",
                        "", false));
                config.add(new ConfigDatastore(
                        "vacuum_cost_page_miss",
                        "2", "0-10000",
                        DataTypeConfig.Integer.name(),
                        "Vacuum cost for a page not found in the buffer cache",
                        "", false));
                config.add(new ConfigDatastore(
                        "vacuum_cost_page_hit",
                        "1", "0-10000",
                        DataTypeConfig.Integer.name(),
                        "Vacuum cost for a page found in the buffer cache",
                        "", false));
                config.add(new ConfigDatastore(
                        "vacuum_cost_page_dirty",
                        "20", "0-10000",
                        DataTypeConfig.Integer.name(),
                        "Vacuum cost for a page dirtied by vacuum",
                        "", false));
                config.add(new ConfigDatastore(
                        "vacuum_cost_limit",
                        "200", "1-10000",
                        DataTypeConfig.Integer.name(),
                        "Vacuum cost amount available before napping",
                        "", false));
                config.add(new ConfigDatastore(
                        "vacuum_cost_delay",
                        "0", "0-100",
                        DataTypeConfig.Integer.name(),
                        "Vacuum cost delay in milliseconds",
                        "ms", false));
                config.add(new ConfigDatastore(
                        "track_functions",
                        "none", "none,pl,all",
                        DataTypeConfig.Enum.name(),
                        "Collects function-level statistics on database activity",
                        "", false));
                config.add(new ConfigDatastore(
                        "track_activity_query_size",
                        "1024", "100-1048576",
                        DataTypeConfig.Integer.name(),
                        "Sets the size reserved for pg_stat_activity.query, in bytes",
                        "B", false));
                config.add(new ConfigDatastore(
                        "timezone_abbreviations",
                        "", "",
                        DataTypeConfig.String.name(),
                        "Selects a file of time zone abbreviations",
                        "", false));
                config.add(new ConfigDatastore(
                        "log_min_duration_statement",
                        "-1", "-1-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the minimum execution time above which statements will be logged",
                        "", false));
                config.add(new ConfigDatastore(
                        "TimeZone",
                        "GMT", "",
                        DataTypeConfig.String.name(),
                        "Sets the time zone for displaying and interpreting time stamps",
                        "", false));
                config.add(new ConfigDatastore(
                        "temp_tablespaces",
                        "", "",
                        DataTypeConfig.String.name(),
                        "Sets the tablespace(s) to use for temporary tables and sort files",
                        "", false));
                config.add(new ConfigDatastore(
                        "temp_file_limit",
                        "-1", "-1-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Limits the total size of all temporary files used by each process",
                        "KB", false));
                config.add(new ConfigDatastore(
                        "temp_buffers",
                        "1024", "100-1073741823",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum number of temporary buffers used by each session",
                        "8kB", false));
                config.add(new ConfigDatastore(
                        "tcp_keepalives_interval",
                        "0", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Time between TCP keepalive retransmits",
                        "s", false));
                config.add(new ConfigDatastore(
                        "tcp_keepalives_idle",
                        "0", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Maximum number of TCP keepalive retransmits",
                        "", false));
                config.add(new ConfigDatastore(
                        "synchronous_standby_names",
                        "", "",
                        DataTypeConfig.String.name(),
                        "Number of synchronous standbys and list of names of potential synchronous ones",
                        "", false));
                config.add(new ConfigDatastore(
                        "tcp_keepalives_count",
                        "0", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Maximum number of TCP keepalive retransmits",
                        "", false));
                config.add(new ConfigDatastore(
                        "superuser_reserved_connections",
                        "3", "0-262143",
                        DataTypeConfig.Integer.name(),
                        "Sets the number of connection slots reserved for superusers",
                        "", false));
                config.add(new ConfigDatastore(
                        "statement_timeout",
                        "0", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum allowed duration of any statement",
                        "ms", false));
                config.add(new ConfigDatastore(
                        "shared_buffers",
                        "1024", "16-1073741823",
                        DataTypeConfig.Integer.name(),
                        "Sets the number of shared memory buffers used by the server",
                        "8kB", false));
                config.add(new ConfigDatastore(
                        "session_replication_role",
                        "origin", "origin,replica,local",
                        DataTypeConfig.Enum.name(),
                        "Sets the session's behavior for triggers and rewrite rules",
                        "", false));
                config.add(new ConfigDatastore(
                        "seq_page_cost",
                        "1", "0-1.79769e+308",
                        DataTypeConfig.Double.name(),
                        "Sets the planner's estimate of the cost of a sequentially fetched disk page",
                        "", false));
                config.add(new ConfigDatastore(
                        "search_path",
                        "public", "",
                        DataTypeConfig.String.name(),
                        "Sets the schema search order for names that are not schema-qualified",
                        "", false));
                config.add(new ConfigDatastore(
                        "replacement_sort_tuples",
                        "150000", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum number of tuples to be sorted using replacement selection",
                        "", false));
                config.add(new ConfigDatastore(
                        "random_page_cost",
                        "4", "0-1.79769e+308",
                        DataTypeConfig.Double.name(),
                        "Sets the planner's estimate of the cost of a nonsequentially fetched disk page",
                        "", false));
                config.add(new ConfigDatastore(
                        "min_wal_size",
                        "80", "2-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the minimum size to shrink the WAL to",
                        "", false));
                config.add(new ConfigDatastore(
                        "max_worker_processes",
                        "8", "0-262143",
                        DataTypeConfig.Integer.name(),
                        "Maximum number of concurrent worker processes",
                        "", false));
                config.add(new ConfigDatastore(
                        "max_wal_size",
                        "1024", "2-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the WAL size that triggers a checkpoint",
                        "MB", false));
                config.add(new ConfigDatastore(
                        "max_standby_streaming_delay",
                        "30000", "-1-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum delay before canceling queries when a hot standby server is processing streamed WAL data",
                        "ms", false));
                config.add(new ConfigDatastore(
                        "max_standby_archive_delay",
                        "30000", "-1-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum delay before canceling queries when a hot standby server is processing archived WAL data",
                        "ms", false));
                config.add(new ConfigDatastore(
                        "max_stack_depth",
                        "100", "100-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum stack depth, in kilobytes",
                        "KB", false));
                config.add(new ConfigDatastore(
                        "max_prepared_transactions",
                        "0", "0-262143",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum number of simultaneously prepared transactions",
                        "", false));
                config.add(new ConfigDatastore(
                        "max_pred_locks_per_transaction",
                        "64", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum number of predicate locks per transaction",
                        "", false));
                config.add(new ConfigDatastore(
                        "max_locks_per_transaction",
                        "64", "10-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum number of locks per transaction",
                        "", false));
                config.add(new ConfigDatastore(
                        "max_files_per_process",
                        "1000", "25-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum number of simultaneously open files for each server process",
                        "", false));
                config.add(new ConfigDatastore(
                        "max_connections",
                        "100", "1-262143",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum number of concurrent connections",
                        "", false));
                config.add(new ConfigDatastore(
                        "maintenance_work_mem",
                        "65536", "1024-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the maximum memory to be used for maintenance operations",
                        "KB", false));
                config.add(new ConfigDatastore(
                        "log_timezone",
                        "GMT", "",
                        DataTypeConfig.String.name(),
                        "Sets the time zone to use in log messages",
                        "", false));
                config.add(new ConfigDatastore(
                        "log_temp_files",
                        "-1", "-1-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Log the use of temporary files larger than this number of kilobytes",
                        "KB", false));
                config.add(new ConfigDatastore(
                        "log_statement",
                        "none", "none,ddl,mod,all",
                        DataTypeConfig.Enum.name(),
                        "Sets the type of statements logged",
                        "", false));
                config.add(new ConfigDatastore(
                        "log_rotation_size",
                        "10240", "0-2097151",
                        DataTypeConfig.Integer.name(),
                        "Automatic log file rotation will occur after N kilobytes",
                        "KB", false));
                config.add(new ConfigDatastore(
                        "log_rotation_age",
                        "1440", "0-35791394",
                        DataTypeConfig.Integer.name(),
                        "Automatic log file rotation will occur after N minutes",
                        "min", false));
                config.add(new ConfigDatastore(
                        "log_min_messages",
                        "warning", "debug5,debug4,debug3,debug2,debug1,info,notice,warning,error,log,fatal,panic",
                        DataTypeConfig.Enum.name(),
                        "Sets the message levels that are logged",
                        "", false));
                config.add(new ConfigDatastore(
                        "log_min_error_statement",
                        "error", "debug5,debug4,debug3,debug2,debug1,info,notice,warning,error,log,fatal,panic",
                        DataTypeConfig.Enum.name(),
                        "Causes all statements generating error at or above this level to be logged",
                        "", false));
                config.add(new ConfigDatastore(
                        "log_line_prefix",
                        "%m [%p]", "",
                        DataTypeConfig.String.name(),
                        "Controls information prefixed to each log line",
                        "", false));
            }
            case "postgresql:16.2:standalone", "postgresql:16.2:master_slave", "postgresql:16.2:cluster_ha" -> {
                config = FactoryConfigDatastore.loadConfig("postgresql:14.9:standalone");
                config.add(new ConfigDatastore(
                        "archive_library",
                        "", "",
                        DataTypeConfig.String.name(),
                        "Sets the library that will be called to archive a WAL file",
                        "", false));
                config.add(new ConfigDatastore(
                        "createrole_self_grant",
                        "", "",
                        DataTypeConfig.String.name(),
                        "Sets whether a CREATEROLE user automatically grants the role to themselves, and with which options",
                        "", false));
                config.add(new ConfigDatastore(
                        "debug_io_direct",
                        "", "",
                        DataTypeConfig.String.name(),
                        "Use direct I/O for file access",
                        "", true));
                config.add(new ConfigDatastore(
                        "debug_logical_replication_streaming",
                        "buffered", "buffered,immediate",
                        DataTypeConfig.Enum.name(),
                        "Forces immediate streaming or serialization of changes in large transactions",
                        "", false));
                config.add(new ConfigDatastore(
                        "debug_parallel_query",
                        "off", "off,on,regress",
                        DataTypeConfig.Enum.name(),
                        "Forces the planner's use parallel query nodes",
                        "", false));
                config.add(new ConfigDatastore(
                        "enable_presorted_aggregate",
                        "on", "on,off",
                        DataTypeConfig.Enum.name(),
                        "Enables the planner's ability to produce plans that provide presorted input for ORDER BY / DISTINCT aggregate functions",
                        "", false));
                config.add(new ConfigDatastore(
                        "gss_accept_delegation",
                        "off", "on,off",
                        DataTypeConfig.Enum.name(),
                        "Sets whether GSSAPI delegation should be accepted from the client",
                        "", false));
                config.add(new ConfigDatastore(
                        "icu_validation_level",
                        "warning", "disabled,debug5,debug4,debug3,debug2,debug1,log,notice,warning,error",
                        DataTypeConfig.Enum.name(),
                        "Log level for reporting invalid ICU locale strings",
                        "", false));
                config.add(new ConfigDatastore(
                        "log_startup_progress_interval",
                        "10000", "0-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Time between progress updates for long-running startup operations",
                        "milliseconds", false));
                config.add(new ConfigDatastore(
                        "max_parallel_apply_workers_per_subscription",
                        "2", "0-1024",
                        DataTypeConfig.Integer.name(),
                        "Maximum number of parallel apply workers per subscription",
                        "", false));
                config.add(new ConfigDatastore(
                        "recovery_prefetch",
                        "try", "off,on,try",
                        DataTypeConfig.Enum.name(),
                        "Prefetch referenced blocks during recovery",
                        "", false));
                config.add(new ConfigDatastore(
                        "recursive_worktable_factor",
                        "10", "0.001-1e+06",
                        DataTypeConfig.Double.name(),
                        "Sets the planner's estimate of the average size of a recursive query's working table",
                        "", false));
                config.add(new ConfigDatastore(
                        "reserved_connections",
                        "0", "0-262143",
                        DataTypeConfig.Integer.name(),
                        "Sets the number of connection slots reserved for roles with privileges of pg_use_reserved_connections",
                        "", true));
                config.add(new ConfigDatastore(
                        "scram_iterations",
                        "4096", "1-2147483647",
                        DataTypeConfig.Integer.name(),
                        "Sets the iteration count for SCRAM secret generation",
                        "", false));
                config.add(new ConfigDatastore(
                        "send_abort_for_crash",
                        "off", "on,off",
                        DataTypeConfig.Enum.name(),
                        "Send SIGABRT not SIGQUIT to child processes after backend crash",
                        "", false));
                config.add(new ConfigDatastore(
                        "send_abort_for_kill",
                        "off", "on,off",
                        DataTypeConfig.Enum.name(),
                        "Send SIGABRT not SIGKILL to stuck child processes",
                        "", false));
                config.add(new ConfigDatastore(
                        "stats_fetch_consistency",
                        "cache", "none,cache,snapshot",
                        DataTypeConfig.Enum.name(),
                        "Sets the consistency of accesses to statistics data",
                        "", false));
                config.add(new ConfigDatastore(
                        "vacuum_buffer_usage_limit",
                        "256", "0-16777216",
                        DataTypeConfig.Integer.name(),
                        "Sets the buffer pool size for VACUUM, ANALYZE, and autovacuum",
                        "KB", false));
                config.add(new ConfigDatastore(
                        "wal_decode_buffer_size",
                        "524288", "65536-1073741823",
                        DataTypeConfig.Integer.name(),
                        "Buffer size for reading ahead in the WAL during recovery",
                        "B", true));
            }
            case "redis:6.0:standalone", "redis:7.0:standalone" -> {
                config.add(new FactoryConfigDatastore.ConfigDatastore("timeout", "0", "0-7200", DataTypeConfig.Integer.name(), "The maximum amount of time (in seconds) a connection between the a client and the DCS instance can be allowed to remain idle before the connection is terminated. A setting of 0 means that this function is disabled.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("hash-max-ziplist-entries", "512", "1-10000", DataTypeConfig.Integer.name(), "The maximum number of hashes that can be encoded using ziplist, a data structure optimized to reduce memory use.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("hash-max-ziplist-value", "64", "1-10000", DataTypeConfig.Integer.name(), "The largest value allowed for a hash encoded using ziplist, a special data structure optimized for memory use.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("set-max-intset-entries", "512", "1-10000", DataTypeConfig.Integer.name(), "If a set is composed entirely of strings that are integers in radix 10 within the range of 64 bit signed integers, sets are encoded using intset, a data structure optimized for memory use.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("zset-max-ziplist-entries", "128", "1-10000", DataTypeConfig.Integer.name(), "The maximum number of sorted set entries allowed before they are encoded using ziplist, a data structure optimized for memory use.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("zset-max-ziplist-value", "64", "1-10000", DataTypeConfig.Integer.name(), "The maximum length allowed for a sorted set before it is encoded using ziplist, a data structure optimized for memory use.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("latency-monitor-threshold", "0", "0-86400000", DataTypeConfig.Integer.name(), """
                        The minimum amount of latency that will be logged as latency spikes
                        Set to 0: Latency monitoring is disabled.
                        Set to more than 0: All with at least this many ms of latency will be logged.
                        """, null, false));

                config.add(new FactoryConfigDatastore.ConfigDatastore("notify-keyspace-events", "Ex", "([KE]+([A]|[g$lshzxe]+)){0,11}", DataTypeConfig.Regular.name(), """
                        Controls which keyspace events notifications are enabled for. If this parameter is left empty, keyspace event notification is disabled. A string of different values can be used to enable notifications for multiple event types: Possible values include:
                        K: Keyspace events, published with the __keyspace@__ prefix
                        E: Keyevent events, published with __keyevent@__ prefix
                        g: Generic commands (non-type specific) such as DEL, EXPIRE, and RENAME
                        $: String commands
                        l: List commands
                        s: Set commands
                        h: Hash commands
                        z: Sorted set commands
                        x: Expired events (events generated every time a key expires)
                        e: Evicted events (events generated when a key is evicted for maxmemory)
                        A: Alias for "g$lshzxe", so that the "AKE" string means all the events.
                        For example, the value Kl means that Redis can notify Pub/Sub clients about keyspace events and list commands. The parameter setting must contain at least a "K" or "E". "A" cannot be selected together with "g$lshzxe", and duplicate characters are not allowed.
                        """, null, false));

                config.add(new FactoryConfigDatastore.ConfigDatastore("slowlog-log-slower-than", "10000", "0-1000000", DataTypeConfig.Integer.name(), "The maximum time allowed, in microseconds, for command execution. If this threshold is exceeded, Slow Queries will record the command.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("slowlog-max-len", "128", "0-1000", DataTypeConfig.Integer.name(), "The maximum allowed number of slow queries. Slow queries consume memory, but you can reclaim this memory by running the SLOWLOG RESET command.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("lua-time-limit", "5000", "100-5000", DataTypeConfig.Integer.name(), "Maximum time allowed for executing a Lua script (in milliseconds)", null, false));
            }
            case "redis:6.0:master_slave", "redis:7.0:master_slave" -> {
                config.add(new FactoryConfigDatastore.ConfigDatastore("timeout", "0", "0-7200", DataTypeConfig.Integer.name(), "Close the connection after a client is idle for N seconds (0 to disable)", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("maxmemory-policy", "volatile-lru", "volatile-lru,allkeys-lru,volatile-lfu,allkeys-lfu,volatile-random,allkeys-random,volatile-ttl,noeviction", DataTypeConfig.Enum.name(), "How Redis will select what to remove when maxmemory is reached, You can select among five behaviors: volatile-lru : remove the key with an expire set using an LRU algorithm; allkeys-lru : remove any key according to the LRU algorithm .volatile-lfu:remove the key with an expire set using an LFU algorithm. allkeys-lfu:remove any key according to the LFU algorithm volatile-random: remove a random key with an expire set allkeys-random: remove a random key, any key volatile-ttl : remove the key with the nearest expire time (minor TTL) noeviction : don't expire at all, just return an error on write operations", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("hash-max-ziplist-entries", "512", "1-10000", DataTypeConfig.Integer.name(), "Hashes are encoded using a memory efficient data structure when they have a small number of entries", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("hash-max-ziplist-value", "64", "1-10000", DataTypeConfig.Integer.name(), "Hashes are encoded using a memory efficient data structure when the biggest entry does not exceed a given threshold", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("set-max-intset-entries", "512", "1-10000", DataTypeConfig.Integer.name(), "When a set is composed of just strings that happen to be integers in radix 10 in the range of 64 bit signed integers.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("zset-max-ziplist-entries", "128", "1-10000", DataTypeConfig.Integer.name(), "Sorted sets are encoded using a memory efficient data structure when they have a small number of entries", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("zset-max-ziplist-value", "64", "1-10000", DataTypeConfig.Integer.name(), "Sorted sets are encoded using a memory efficient data structure when the biggest entry does not exceed a given threshold", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("latency-monitor-threshold", "0", "0-86400000", DataTypeConfig.Integer.name(), "Only events that run in more time than the configured latency-monitor-threshold will be logged as latency spikes. If latency-monitor-threshold is set to 0, latency monitoring is disabled. If latency-monitor-threshold is set to a value greater than 0, all events blocking the server for a time equal to or greater than the configured latency-monitor-threshold will be logged.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("notify-keyspace-events", "Ex", "([KE]+([A]|[g$lshzxe]+)){0,11}", DataTypeConfig.Regular.name(), "Redis can notify Pub or Sub clients about events happening in the key space", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("repl-backlog-size", "1048576", "16384-1073741824", DataTypeConfig.Integer.name(), "The replication backlog size in bytes for PSYNC. This is the size of the buffer which accumulates slave data when slave is disconnected for some time, so that when slave reconnects again, only transfer the portion of data which the slave missed.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("repl-backlog-ttl", "3600", "0-604800", DataTypeConfig.Integer.name(), "The amount of time in seconds after the master no longer have any slaves connected for the master to free the replication backlog. A value of 0 means to never release the backlog.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("appendfsync", "no", "no,always,everysec", DataTypeConfig.Enum.name(), """
                        The fsync() call tells the Operating System to actually write data on disk instead of waiting for more data in the output buffer. Some OS will really flush data on disk, some other OS will just try to do it ASAP. Redis supports three different modes:
                        no: don't fsync, just let the OS flush the data when it wants. Faster.
                        always: fsync after every write to the append only log. Slow, Safest.
                        everysec: fsync only one time every second. Compromise.""", null, false));

                config.add(new FactoryConfigDatastore.ConfigDatastore("appendonly", "yes", "yes,no", DataTypeConfig.Enum.name(), "Configuration item for AOF persistence", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("slowlog-log-slower-than", "10000", "0-1000000", DataTypeConfig.Integer.name(), "The Redis Slow Log is a system to log queries that exceeded a specified execution time. slowlog-log-slower-than tells Redis what is the execution time, in microseconds, to exceed in order for the command to get logged ", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("slowlog-max-len", "128", "0-1000", DataTypeConfig.Integer.name(), "The Redis Slow Log is a system to log queries that exceeded a specified execution time. slowlog-log-slower-than tells Redis what is the execution time, in microseconds, to exceed in order for the command to get logged", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("lua-time-limit", "5000", "100-5000", DataTypeConfig.Integer.name(), "Max execution time of a Lua script in milliseconds.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("repl-timeout", "60", "30-3600", DataTypeConfig.Integer.name(), "Replication timeout in seconds.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("proto-max-bulk-len", "536870912", "1048576-536870912", DataTypeConfig.Integer.name(), "Max bulk request size in bytes.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("master-read-only", "no", "yes,no", DataTypeConfig.Enum.name(), "Set redis to read only state and all write commands will fail.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("client-output-buffer-limit-slave-soft-seconds", "60", "0-60", DataTypeConfig.Integer.name(), "Set redis to read only state and all write commands will fail.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("active-expire-num", "20", "1-1000", DataTypeConfig.Integer.name(), "How many keys can be freed by expire cycle.", null, false));
            }
            case "redis:6.0:cluster", "redis:7.0:cluster" -> {
                config.add(new FactoryConfigDatastore.ConfigDatastore("timeout", "0", "0-7200", "Interger", "Close the connection after a client is idle for N seconds (0 to disable)", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("maxmemory-policy", "volatile-lru", "volatile-lru,allkeys-lru,volatile-lfu,allkeys-lfu,volatile-random,allkeys-random,volatile-ttl,noeviction", "Enum", "How Redis will select what to remove when maxmemory is reached, You can select among five behaviors: volatile-lru : remove the key with an expire set using an LRU algorithm; allkeys-lru : remove any key according to the LRU algorithm .volatile-lfu:remove the key with an expire set using an LFU algorithm. allkeys-lfu:remove any key according to the LFU algorithm volatile-random: remove a random key with an expire set allkeys-random: remove a random key, any key volatile-ttl : remove the key with the nearest expire time (minor TTL) noeviction : don't expire at all, just return an error on write operations", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("hash-max-ziplist-entries", "512", "1-10000", "Interger", "Hashes are encoded using a memory efficient data structure when they have a small number of entries", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("hash-max-ziplist-value", "64", "1-10000", "Interger", "Hashes are encoded using a memory efficient data structure when the biggest entry does not exceed a given threshold", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("set-max-intset-entries", "512", "1-10000", "Interger", "When a set is composed of just strings that happen to be integers in radix 10 in the range of 64 bit signed integers.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("zset-max-ziplist-entries", "128", "1-10000", "Interger", "Sorted sets are encoded using a memory efficient data structure when they have a small number of entries", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("zset-max-ziplist-value", "64", "1-10000", "Interger", "Sorted sets are encoded using a memory efficient data structure when the biggest entry does not exceed a given threshold", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("latency-monitor-threshold", "0", "0-86400000", "Interger", "Only events that run in more time than the configured latency-monitor-threshold will be logged as latency spikes. If latency-monitor-threshold is set to 0, latency monitoring is disabled. If latency-monitor-threshold is set to a value greater than 0, all events blocking the server for a time equal to or greater than the configured latency-monitor-threshold will be logged.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("notify-keyspace-events", "Ex", "([KE]+([A]|[g$lshzxe]+)){0,11}", "regular", "Redis can notify Pub or Sub clients about events happening in the key space", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("repl-backlog-size", "1048576", "16384-1073741824", "Interger", "The replication backlog size in bytes for PSYNC. This is the size of the buffer which accumulates slave data when slave is disconnected for some time, so that when slave reconnects again, only transfer the portion of data which the slave missed.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("repl-backlog-ttl", "3600", "0-604800", "Interger", "The amount of time in seconds after the master no longer have any slaves connected for the master to free the replication backlog. A value of 0 means to never release the backlog.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("appendfsync", "no", "no,always,everysec", "Enum", """
                                The fsync() call tells the Operating System to actually write data on disk instead of waiting for more data in the output buffer. Some OS will really flush data on disk, some other OS will just try to do it ASAP.
                                Redis supports three different modes:
                        no: don't fsync, just let the OS flush the data when it wants. Faster.
                        always: fsync after every write to the append only log. Slow, Safest.
                                everysec: fsync only one time every second. Compromise.""", null, false));

                config.add(new FactoryConfigDatastore.ConfigDatastore("appendonly", "yes", "yes,no", "Enum", "Configuration item for AOF persistence", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("slowlog-log-slower-than", "10000", "0-1000000", "Interger", """
                                The Redis Slow Log is a system to log queries that exceeded a specified execution time.
                                slowlog-log-slower-than tells Redis what is the execution time, in microseconds, to exceed in order for the
                        command to get logged
                        """, null, false));

                config.add(new FactoryConfigDatastore.ConfigDatastore("slowlog-max-len", "128", "0-1000", "Interger", """
                        The Redis Slow Log is a system to log queries that exceeded a specified execution time.
                        slowlog-log-slower-than tells Redis what is the execution time, in microseconds, to exceed in order for the command to get logged
                        """, null, false));

                config.add(new FactoryConfigDatastore.ConfigDatastore("lua-time-limit", "5000", "100-5000", "Interger", "Max execution time of a Lua script in milliseconds.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("repl-timeout", "60", "30-3600", "Interger", "Replication timeout in seconds.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("proto-max-bulk-len", "536870912", "1048576-536870912", "Interger", "Max bulk request size in bytes.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("master-read-only", "no", "yes,no", "Enum", "Set redis to read only state and all write commands will fail.", null, false));
                config.add(new FactoryConfigDatastore.ConfigDatastore("client-output-buffer-limit-slave-soft-seconds", "60", "0-60", "Interger", "Set redis to read only state and all write commands will fail.", null, false));

            }
            case "mongodb:6.0:standalone", "mongodb:7.0:standalone" -> {
                config.add(new ConfigDatastore("security.authorization",
                        "disabled",
                        "enabled,disabled",
                        DataTypeConfig.Enum.name(),
                        "Enable or disable Role-Based Access Control (RBAC) to govern each user's access to database resources and operations.",
                        null, false));
            }
            case "mongodb:6.0:replica_set", "mongodb:7.0:replica_set" -> {
                config.add(new ConfigDatastore("security.authorization",
                        "disabled",
                        "enabled,disabled",
                        DataTypeConfig.Enum.name(),
                        "Enable or disable Role-Based Access Control (RBAC) to govern each user's access to database resources and operations.",
                        null, false));
            }
            case "kafka:3.7:single_node", "kafka:3.7:cluster" -> {
                config.add(new ConfigDatastore("log.flush.interval.messages",
                        "9223372036854775807",
                        "1-",
                        DataTypeConfig.Long.name(),
                        "Number of messages that accumulate on a log partition before messages are flushed to disk.",
                        null, false));

                config.add(new ConfigDatastore("auto.create.topics.enable",
                        "false",
                        "true,false",
                        DataTypeConfig.Boolean.name(),
                        "Enables autocreation of a topic on the server.",
                        null, false));

                config.add(new ConfigDatastore("remote.log.reader.threads",
                        "10",
                        "1-",
                        DataTypeConfig.Integer.name(),
                        "Remote log reader thread pool size, which is used in scheduling tasks to fetch data from remote storage.",
                        null, false));

                config.add(new ConfigDatastore("log.flush.interval.ms",
                        "0",
                        "0-",
                        DataTypeConfig.Long.name(),
                        "Maximum time in milliseconds that a message in any topic remains in memory before flushed to disk. If you don't set this value, the value in log.flush.scheduler.interval.ms is used. The minimum value is 0.",
                        null, false));

                config.add(new ConfigDatastore("replica.socket.receive.buffer.bytes",
                        "65536",
                        "0-",
                        DataTypeConfig.Integer.name(),
                        "The socket receive buffer for network requests.",
                        null, false));

                config.add(new ConfigDatastore("min.insync.replicas",
                        "1",
                        "1-",
                        DataTypeConfig.Integer.name(),
                        "When a producer sets the value of acks (acknowledgement producer gets from Kafka broker) to \"all\" (or \"-1\"), the value in min.insync.replicas specifies the minimum number of replicas that must acknowledge a write for the write to be considered successful. If this value doesn't meet this minimum, the producer raises an exception (either NotEnoughReplicas or NotEnoughReplicasAfterAppend).\n" +
                                "\n" +
                                "When you use the values in min.insync.replicas and acks together, you can enforce greater durability guarantees. For example, you might create a topic with a replication factor of 3, set min.insync.replicas to 2, and produce with acks of \"all\". This ensures that the producer raises an exception if a majority of replicas don't receive a write.",
                        null, false));

                config.add(new ConfigDatastore("zookeeper.connection.timeout.ms",
                        "18000",
                        "6000-18000",
                        DataTypeConfig.Integer.name(),
                        "ZooKeeper mode clusters. Maximum time that the client waits to establish a connection to ZooKeeper. If you don't set this value, the value in zookeeper.session.timeout.ms is used.",
                        null, false));

                config.add(new ConfigDatastore("num.recovery.threads.per.data.dir",
                        "1",
                        "1-",
                        DataTypeConfig.Integer.name(),
                        "The number of threads per data directory to be used to recover logs at startup and and to flush them at shutdown.",
                        null, false));

                config.add(new ConfigDatastore("local.retention.ms",
                        "-2",
                        "-2-",
                        DataTypeConfig.Long.name(),
                        "The number of milliseconds to keep the local log segment before it gets deleted. Default value is -2, it represents `retention.ms` value is to be used. The effective value should always be less than or equal to `retention.ms` value.",
                        null, false));

                config.add(new ConfigDatastore("log.cleanup.policy",
                        "delete",
                        "compact,delete",
                        DataTypeConfig.Enum.name(),
                        "The default cleanup policy for segments beyond the retention window. A comma-separated list of valid policies. Valid policies are delete and compact. For Tiered Storage enabled clusters, valid policy is delete only.",
                        null, false));

                config.add(new ConfigDatastore("default.replication.factor",
                        "1",
                        "1-",
                        DataTypeConfig.Integer.name(),
                        "The default replication factors for automatically created topics.",
                        null, false));

                config.add(new ConfigDatastore("replica.selector.class",
                        "null",
                        "",
                        DataTypeConfig.String.name(),
                        "The fully qualified class name that implements ReplicaSelector. This is used by the broker to find the preferred read replica. By default, we use an implementation that returns the leader.",
                        null, false));

                config.add(new ConfigDatastore("message.max.bytes",
                        "1048588",
                        "0-",
                        DataTypeConfig.Integer.name(),
                        "Largest record batch size that Kafka allows. If you increase this value and there are consumers older than 0.10.2, you must also increase the fetch size of the consumers so that they can fetch record batches this large.\n" +
                                "\n" +
                                "The latest message format version always groups messages into batches for efficiency. Previous message format versions don't group uncompressed records into batches, and in such a case, this limit only applies to a single record.\n" +
                                "\n" +
                                "You can set this value per topic with the topic level max.message.bytes config.",
                        null, false));

                config.add(new ConfigDatastore("transactional.id.expiration.ms",
                        "604800000",
                        "1-",
                        DataTypeConfig.Integer.name(),
                        "The time in ms that the transaction coordinator will wait without receiving any transaction status updates for the current transaction before expiring its transactional id. Transactional IDs will not expire while a the transaction is still ongoing.",
                        null, false));

                config.add(new ConfigDatastore("transaction.state.log.replication.factor",
                        "3",
                        "1-",
                        DataTypeConfig.Integer.name(),
                        "description",
                        null, false));

//                config.add(new ConfigDatastore("num.io.threads",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("group.initial.rebalance.delay.ms",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("local.retention.bytes",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("delete.topic.enable",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("log.segment.bytes",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("group.max.session.timeout.ms",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("replica.fetch.response.max.bytes",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("log.retention.minutes",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("num.replica.fetchers",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("allow.everyone.if.no.acl.found",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("compression.type",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("log.retention.ms",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("log.message.timestamp.difference.max.ms",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("remote.storage.enable",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("log.cleaner.delete.retention.ms",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("offsets.topic.replication.factor",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("transaction.state.log.min.isr",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("auto.leader.rebalance.enable",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("log.cleaner.min.cleanable.ratio",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("replica.lag.time.max.ms",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("socket.request.max.bytes",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("zookeeper.session.timeout.ms",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("max.incremental.fetch.session.cache.slots",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("num.network.threads",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("log.retention.bytes",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("log.message.timestamp.type",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("retention.ms",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("zookeeper.set.acl",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("connections.max.idle.ms",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("offsets.retention.minutes",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("socket.send.buffer.bytes",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("log.retention.hours",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("remote.log.msk.disable.policy",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("num.partitions",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("socket.receive.buffer.bytes",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("log.roll.ms",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("unclean.leader.election.enable",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("group.min.session.timeout.ms",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("transaction.max.timeout.ms",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("replica.fetch.max.bytes",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));
//
//                config.add(new ConfigDatastore("leader.imbalance.per.broker.percentage",
//                        "default_value",
//                        "range_value",
//                        DataTypeConfig.XXX.name(),
//                        "description",
//                        null, false));

            }
            default -> throw new AppException(new ErrorResponse("datastore invalid : " + datastoreNameVersion));
        }
        return config;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConfigDatastore {
        private String paramName;
        private String defaultValue;
        private String rangeValue;
        private String typeValue;
        private String description;
        private String unit;
        private Boolean needRestart = false;
    }

}
