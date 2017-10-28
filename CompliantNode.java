import java.util.ArrayList;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    double m_p_graph;
    double m_p_malicious;
    double m_p_txDistribution;
    int m_numRounds;

    boolean[] m_followees;

    Set<Transaction> m_pendingTransactions;

    Set<Transaction> m_TxIwantToSend;

    Set<Transaction> m_incomingSet;


    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.m_p_graph = p_graph;
        this.m_p_malicious = p_malicious;
        this.m_p_txDistribution = p_txDistribution;
        this.m_numRounds = numRounds;

        this.m_incomingSet = new HashSet<Transaction>();
    }

    public void setFollowees(boolean[] followees) {
        this.m_followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.m_pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        if (this.m_incomingSet.size() > 0) {
            Transaction[] inc = this.m_incomingSet.toArray(new Transaction[this.m_incomingSet.size()]);
            for (int i = 0; i < inc.length; i++) {
                this.m_pendingTransactions.add(inc[i]);
            }
        }
        return this.m_pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        if (candidates.size() > 0 ) {
            Candidate[] cands = candidates.toArray( new Candidate[candidates.size()]);

            for(int i= 0; i < cands.length; i++) {
                if (this.isNodeCanSendTx(cands[i].sender) && false == this.isTxAlreadyInBlock(cands[i].tx) ) {
                    this.m_incomingSet.add(cands[i].tx);
                }
            }
        }
    }


    public boolean isNodeCanSendTx(int NodeCode){
        boolean canSend = false;
        canSend = NodeCode < m_followees.length;
        return canSend && m_followees[NodeCode];
    }

    public boolean isTxAlreadyInBlock(Transaction txCandidate){
        return this.m_pendingTransactions.contains(txCandidate) && this.m_incomingSet.contains(txCandidate);
    }
}
