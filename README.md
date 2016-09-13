Assignment 1:
Information_Retrieval_Models_Elastic_Search

Implement and compare various retrieval systems using vector space models and language models.

Assignment 2:
Information_Retrieval_Models_with_manual_index

Implement your own index to take the place of elasticsearch in the HW1 code, and index the document collection used for HW1. Your index should be able to handle large numbers of documents and terms without using excessive memory or disk I/O.

This involves writing two programs:

    A tokenizer and indexer
    An updated version of your HW1 ranker which uses your inverted index

Assignment 3:
Focused_Web_Crawler

You will write a web crawler, and crawl Internet documents to construct a document collection focused on a particular topic. Your crawler must conform strictly to a particular politeness policy. Once the documents are crawled, you will pool them together.

Form a team of three students with your classmates. Your team will be assigned a single query with few associated seed URLs. You will each crawl web pages starting from a different seed URL. When you have each collected your individual documents, you will pool them together, index them and implement search.


Assignment 4:
PageRank and HITS

PageRank
• Implement the PageRank algorithm to compute PageRank score of every page in the already 60000 webpage crawl data based on the inlinks and outlinks of each page.
• List the top 500 pages by the PageRank score.

HITS
• Implement HITS algorithm to compute Hubs and Authority score for the webpage crawl data
• List the top 500 hub webpages by the Hub score.
• List the top 500 authority webpages by the Authority score.

Assignment 5:
IR_Evaluation_with_Trec_Eval

You will be given queries for your topical crawl. Manual relevance assessments have to be collected, using your vertical search engine and a web interface.
You will have to code up the IR evaluation measures, essentially rewriting treceval.

Assignment 6:
Machine_Learning_using_LibLinear

In this assignment, you will represent documents as numerical features, and apply machine learning to obtain retrieval ranked lists. The data is the AP89 collection we used for HW1.

Data

Restrict the data to documents present in the QREL. That is, for each of the 25 queries only consider documents that have a qrel assessment. You should have about 14193 documents; some of the docIDs will appear multiple times for multiple queries, while most documents will not appear at all.

Split the data randomly into 20 “training” quereis and 5 “testing” queries.

Train a learning algorithm

Using the “train” queries static matrix, train a learner to compute a model relating labels to the features. You can use a learning library like SciPy/NumPy, C4.5, Weka, LibLinear, SVM Light, etc. The easiest models are linear regression and decision trees.
Test the model

For each of the 5 testing queries:

   • Run the model to obtain scores
   • Treat the scores as coming from an IR function, and rank the documents
   • Format the results as in HW1
   • Run treceval and report evaluation as “testingperformance”.


Assignment 7:
Unigram,Bigram_and_Spam_classifier

Build a Spam Classifier using Machine Learning and ElasticSearch.

Part1: Manual Spam Features
Manually create a list of ngrams (unigrams, bigrams, trigrams, etc) that you think are related to spam. For example : “free” , “win”, “click here”, etc. These will be the features (columns) of the data matrix.

Train a learning algorithm

The label, or outcome, or target are the spam annotation “yes” / “no” or you can replace that with 1/0.

Using the “train” queries static matrix, train a learner to compute a model relating labels to the features on TRAIN set. You can use a learning library like SciPy/NumPy, C4.5, Weka, LibLinear, SVM Light, etc. The easiest models are linear regression and decision trees.
Test the spam model

Test the model on TEST set. You will have to create a testing data matrix with feature values in the same exact way as you created the training matrix: use ElasticSearch to query for your features, use the scores are feature values.

    • Run the model to obtain scores
    • Treat the scores as coming from an IR function, and rank the documents
    • Display first few “spam” documents and visually inspect them. You should have these ready for demo. IMPORTANT : Since they are likely to be spam, if you display these in a browser, you should turn off javascript execution to protect your computer.

	
Part 2: All unigrams as features
A feature matrix should contain a column/feature for every unigram extracted from training documents. You will have to use a particular data format described in class (note, toy example), since the full matrix becomes too big. Write the matrix and auxiliary files on disk. 

Given the requirements on data cleaning, you should not have too many unigrams, but still many enough to have to use a sparse representation.
Extracting all unigrams using Elastic Search calls

This is no different than part1 in terms of the ES calls, but you'd have to first generate a list with all unigrams.
Training and testing
Once the feature matrices are ready (one for training, the second for testing), run either LibLinear Regression (with sparse input)  or a learning algorithm implemented by us to take advantage of the sparse data representations.	

Assignment 8:
Clustering_and_Topic_Models

A) Topic Models per Query
For any given query, select a set of documents that is union of top 1000 BM25 docs (for that query) and the qrel docs (for that query). The set will be a little over 1000 docs since there will be considerable overlap.
Then use one of the packages listed below to perform LDA. Your output must contain

    the topics detected, with each topic represented as distribution over words (list the top 20-30 words)
    the documents represented as topic distribution. Each document should have listed the most relevant 10 topics or so

Repeat the process for each one of the 25 queries.

B) LDA-topics and clustering

Run LDA on the entire AP89 collection, with about T=200 topics. Obtain a representation of all documents in these topics.
Then run a clustering-partition algorithm on documents in this topic representation. partition means every document gets assigned to exactly one cluster, like with K-means. You are free to use any clustering library/package. Target K=25 clusters. List each cluster with its documents IDs. You should have an ES index set up so we can look up documents by ID.

How to evaluate: There are about 1831 relevant documents in total. Consider all the pairs of two relevant documents, that is (1831 choose 2). For each pair, count a 1 in the appropriate cell of the following confusion table
