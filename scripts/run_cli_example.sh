set -euro pipefail

mkdir -p tmp

sbt "runMain cli.SophieCli --file examples/cli_buy_sell.sophie --run --ledger tmp/ledger.ndjson --portfolio tmp/portfolio.json --reset-portfolio"