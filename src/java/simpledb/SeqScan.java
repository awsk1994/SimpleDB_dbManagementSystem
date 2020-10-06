package simpledb;

import java.util.*;

/**
 * SeqScan is an implementation of a sequential scan access method that reads
 * each tuple of a table in no particular order (e.g., as they are laid out on
 * disk).
 */
public class SeqScan implements DbIterator {
    private TransactionId _tid;
    private int _tableId;
    private String _tableAlias;

    private TupleDesc _td;
    private DbFile _dbFile;
    private DbFileIterator _dbFileIterator;

    private static final long serialVersionUID = 1L;

    /**
     * Creates a sequential scan over the specified table as a part of the
     * specified transaction.
     *
     * @param tid
     *            The transaction this scan is running as a part of.
     * @param tableid
     *            the table to scan.
     * @param tableAlias
     *            the alias of this table (needed by the parser); the returned
     *            tupleDesc should have fields with name tableAlias.fieldName
     *            (note: this class is not responsible for handling a case where
     *            tableAlias or fieldName are null. It shouldn't crash if they
     *            are, but the resulting name can be null.fieldName,
     *            tableAlias.null, or null.null).
     */
    public SeqScan(TransactionId tid, int tableid, String tableAlias) {
        this._tid = tid;
        this._tableId = tableid;
        this._tableAlias = tableAlias;

        // prefix tuple field with table alias
        updateTupleFieldPrefix(tableid, tableAlias);

        this._dbFile = Database.getCatalog().getDatabaseFile(tableid);
        this._dbFileIterator = _dbFile.iterator(tid);
    }

    private void updateTupleFieldPrefix(int tableid, String tableAlias) {
        TupleDesc origTd = Database.getCatalog().getTupleDesc(tableid);
        String[] fieldAr = new String[origTd.numFields()];
        Type[] typeAr = new Type[origTd.numFields()];
        for (int i = 0; i < origTd.numFields(); i++) {
            fieldAr[i] = String.format("%s.%s", tableAlias, origTd.getFieldName(i));
            typeAr[i] = origTd.getFieldType(i);
        }
        this._td = new TupleDesc(typeAr, fieldAr);
    }

    /**
     * @return
     *       return the table name of the table the operator scans. This should
     *       be the actual name of the table in the catalog of the database
     * */
    public String getTableName() {
        return Database.getCatalog().getTableName(_tableId);
    }

    /**
     * @return Return the alias of the table this operator scans.
     * */
    public String getAlias()
    {
        return _tableAlias;
    }

    /**
     * Reset the tableid, and tableAlias of this operator.
     * @param tableid
     *            the table to scan.
     * @param tableAlias
     *            the alias of this table (needed by the parser); the returned
     *            tupleDesc should have fields with name tableAlias.fieldName
     *            (note: this class is not responsible for handling a case where
     *            tableAlias or fieldName are null. It shouldn't crash if they
     *            are, but the resulting name can be null.fieldName,
     *            tableAlias.null, or null.null).
     */
    public void reset(int tableid, String tableAlias) {
        _tableId = tableid;
        _tableAlias = tableAlias;
        // TODO: update TupleDesc accordingly?
        updateTupleFieldPrefix(tableid, tableAlias);
    }

    public SeqScan(TransactionId tid, int tableid) {
        this(tid, tableid, Database.getCatalog().getTableName(tableid));
    }

    public void open() throws DbException, TransactionAbortedException {
        _dbFileIterator.open();
    }

    /**
     * Returns the TupleDesc with field names from the underlying HeapFile,
     * prefixed with the tableAlias string from the constructor. This prefix
     * becomes useful when joining tables containing a field(s) with the same
     * name.  The alias and name should be separated with a "." character
     * (e.g., "alias.fieldName").
     *
     * @return the TupleDesc with field names from the underlying HeapFile,
     *         prefixed with the tableAlias string from the constructor.
     */
    public TupleDesc getTupleDesc() {
        return _td;
    }

    public boolean hasNext() throws TransactionAbortedException, DbException {
        return _dbFileIterator.hasNext();
    }

    public Tuple next() throws NoSuchElementException,
            TransactionAbortedException, DbException {
        return _dbFileIterator.next();
    }

    public void close() {
        _dbFileIterator.close();
    }

    public void rewind() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        _dbFileIterator.rewind();
    }
}
