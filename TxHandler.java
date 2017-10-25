import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private UTXOPool m_utxopoll;
    private UTXOPool m_localpool;

    public TxHandler(UTXOPool utxoPool) {
        this.m_utxopoll = utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        double  InputValue = 0;
        double  OutValue   = 0;
        boolean OutputsGreaterThanZero = true;
        boolean outputNotINthePool = true;
        boolean sig = true;
        boolean doubleSpendisNotPresent = true;

        UTXOPool tpool = new UTXOPool();

        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();

        for (int i = 0; i < inputs.size(); i++){
            Transaction.Input in = inputs.get(i);
            UTXO out = new UTXO( in.prevTxHash, in.outputIndex);

            doubleSpendisNotPresent = doubleSpendisNotPresent && !tpool.contains(out);

            if (!tpool.contains(out)) {
                tpool.addUTXO(out, tx.getOutput(in.outputIndex));
            }

            Transaction.Output outTran = this.m_utxopoll.getTxOutput(out);
            if (null == outTran) {
                outputNotINthePool = false;
            } else {
                InputValue = InputValue + outTran.value;
                sig = sig && Crypto.verifySignature(outTran.address, tx.getRawDataToSign(i), in.signature);
            }
        }

        for (int j = 0; j < outputs.size(); j++){
            Transaction.Output out = outputs.get(j);
            OutputsGreaterThanZero = OutputsGreaterThanZero && out.value > 0;
            OutValue = OutValue + out.value;
        }
        boolean rez = InputValue >= OutValue && OutputsGreaterThanZero && outputNotINthePool && sig && doubleSpendisNotPresent;

        if (true == rez){
            this.addToPool(tx);
        }
        return rez;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> outs = new ArrayList<Transaction>();
        this.m_localpool = new UTXOPool();

        for(int i = 0; i < possibleTxs.length; i++ ){
            if (this.isValidTx(possibleTxs[i])){
                Transaction pos = possibleTxs[i];
                if (this.noDoubleSpend(pos)){
                    outs.add(pos);
                }
            }
        }
        return outs.toArray(new Transaction[outs.size()]);
    }

    public boolean noDoubleSpend(Transaction tx){
        boolean doubleSpendisNotPresent = true;

        ArrayList<Transaction.Input> inputs = tx.getInputs();

        for (int i = 0; i < inputs.size(); i++){
            Transaction.Input in = inputs.get(i);
            UTXO out = new UTXO( in.prevTxHash, in.outputIndex);

            doubleSpendisNotPresent = doubleSpendisNotPresent && !this.m_localpool.contains(out);

            if (doubleSpendisNotPresent) {
                this.m_localpool.addUTXO(out, tx.getOutput(in.outputIndex));
            }
        }
        return doubleSpendisNotPresent;
    }

    public void addToPool(Transaction tx) {
        for(int j = 0; j < tx.numOutputs(); j++) {
            UTXO utxo= new UTXO( tx.getHash(), j );
            this.m_utxopoll.addUTXO(utxo, tx.getOutput(j) );
        }

        for(int j = 0; j < tx.numInputs(); j++) {
            UTXO utxo= new UTXO( tx.getHash(), j );
            this.m_utxopoll.removeUTXO(utxo);
        }
    }
}
