import java.io.*;
import java.util.Scanner;
import software.amazon.codeguruprofilerjavaagent.Profiler;


public class Solution2 {

   static BufferedWriter output;
   
   public static void main(String[] args) throws Exception {
   new Profiler.Builder()
   .profilingGroupName("Test-1")
   .build().start();
   output = new BufferedWriter(new OutputStreamWriter(System.out, "ASCII"), 4096);
   Scanner in = new Scanner(System.in);

   TwoThreeTree tree = new TwoThreeTree();

   int n = in.nextInt();
   for (int i = 0; i < n; i++) {
       int choice = in.nextInt();
       if (choice == 1) {
           String planet = in.next();
           int fee = in.nextInt();
           insert(planet, fee, tree);
       }
       else if (choice == 2) {
           String planet1 = in.next();
           String planet2 = in.next();
           int inc = in.nextInt();
           if (planet1.compareTo(planet2) > 0) {
               String c = planet1;
               planet1 = planet2;
               planet2 = c;
           }
           addRange(tree.root, planet1, planet2, tree.height, inc);
       }
       else if (choice == 3) {
           String search = in.next();
           int sum = 0;
           search(tree.root, search, tree.height,sum);
       }
   }

   output.flush();
   in.close();
}
   
   

static void insert(String key, int value, TwoThreeTree tree) {
   // insert a key value pair into tree (overwrite existsing value
   // if key is already present)
   
       int h = tree.height;
   
       if (h == -1) {
           LeafNode newLeaf = new LeafNode();
           newLeaf.guide = key;
           newLeaf.value = value;
           tree.root = newLeaf; 
           tree.height = 0;
       }
       else {
           WorkSpace ws = doInsert(key, value, tree.root, h);
   
           if (ws != null && ws.newNode != null) {
           // create a new root
   
               InternalNode newRoot = new InternalNode();
               if (ws.offset == 0) {
               newRoot.child0 = ws.newNode; 
               newRoot.child1 = tree.root;
               }
               else {
               newRoot.child0 = tree.root; 
               newRoot.child1 = ws.newNode;
               }
               resetGuide(newRoot);
               tree.root = newRoot;
               tree.height = h+1;
           }
       }
   }
   
   static WorkSpace doInsert(String key, int value, Node p, int h) {
   // auxiliary recursive routine for insert
   
       if (h == 0) {
           // we're at the leaf level, so compare and 
           // either update value or insert new leaf
   
           LeafNode leaf = (LeafNode) p; //downcast
           int cmp = key.compareTo(leaf.guide);
   
           if (cmp == 0) {
               leaf.value = value; 
               return null;
           }
   
           // create new leaf node and insert into tree
           LeafNode newLeaf = new LeafNode();
           newLeaf.guide = key; 
           newLeaf.value = value;
   
           int offset = (cmp < 0) ? 0 : 1;
           // offset == 0 => newLeaf inserted as left sibling
           // offset == 1 => newLeaf inserted as right sibling
   
           WorkSpace ws = new WorkSpace();
           ws.newNode = newLeaf;
           ws.offset = offset;
           ws.scratch = new Node[4];
   
           return ws;
       }
       else {
           InternalNode q = (InternalNode) p; // downcast
           int pos;
           WorkSpace ws;
   
           if (key.compareTo(q.child0.guide) <= 0) {
               pos = 0;
               //appendChildren(q, 1, q.value);
               q.child0.value += q.value;
               q.value = 0;
               ws = doInsert(key, value, q.child0, h-1);
           }
           else if (key.compareTo(q.child1.guide) <= 0 || q.child2 == null) {
               pos = 1;
               //appendChildren(q, 2, q.value);
               q.child0.value += q.value;
               q.child1.value += q.value;
               q.value = 0;
               ws = doInsert(key, value, q.child1, h-1);
           }
           else {
               //appendChildren(q, 3, q.value);
               q.child0.value += q.value;
               q.child1.value += q.value;
               q.child2.value += q.value;
               q.value = 0;
               pos = 2; 
               ws = doInsert(key, value, q.child2, h-1);
           }
   
           if (ws != null) {
               if (ws.newNode != null) {
               // make ws.newNode child # pos + ws.offset of q
   
               int sz = copyOutChildren(q, ws.scratch);
               insertNode(ws.scratch, ws.newNode, sz, pos + ws.offset);
               if (sz == 2) {
                   ws.newNode = null;
                   ws.guideChanged = resetChildren(q, ws.scratch, 0, 3);
               }
               else {
                   ws.newNode = new InternalNode();
                   ws.offset = 1;
                   resetChildren(q, ws.scratch, 0, 2);
                   resetChildren((InternalNode) ws.newNode, ws.scratch, 2, 2);
               }
               }
               else if (ws.guideChanged) {
               ws.guideChanged = resetGuide(q);
               }
           }
   
           return ws;
       }
   }
   
   
   static int copyOutChildren(InternalNode q, Node[] x) {
   // copy children of q into x, and return # of children
   
       int sz = 2;
       x[0] = q.child0; x[1] = q.child1;
       if (q.child2 != null) {
           x[2] = q.child2; 
           sz = 3;
       }
       return sz;
   }
   
   static void insertNode(Node[] x, Node p, int sz, int pos) {
   // insert p in x[0..sz) at position pos,
   // moving existing extries to the right
   
       for (int i = sz; i > pos; i--)
           x[i] = x[i-1];
   
       x[pos] = p;
   }
   
   static boolean resetGuide(InternalNode q) {
   // reset q.guide, and return true if it changes.
   
       String oldGuide = q.guide;
       if (q.child2 != null)
           q.guide = q.child2.guide;
       else
           q.guide = q.child1.guide;
   
       return q.guide != oldGuide;
   }
   
   
   static boolean resetChildren(InternalNode q, Node[] x, int pos, int sz) {
   // reset q's children to x[pos..pos+sz), where sz is 2 or 3.
   // also resets guide, and returns the result of that
   
       q.child0 = x[pos]; 
       q.child1 = x[pos+1];
   
       if (sz == 3) 
           q.child2 = x[pos+2];
       else
           q.child2 = null;
   
       return resetGuide(q);
   }

   static void search(Node init, String x, int h, int sum) throws Exception {
       if (h == 0) {
           if (x.equals(init.guide)) {
               init.value += sum;
               String printStr = init.value+" ";
               output.write(printStr+"\n");
           }
           else {
               String printStr = "-1";
               output.write(printStr+"\n");
           }
       }
       else {
           InternalNode p = (InternalNode) init;
           if (x.compareTo(p.child0.guide) <= 0) {
               sum += p.value;
               search(p.child0, x, h-1, sum);
           }
           else if ((p.child2.equals(null)) || (x.compareTo(p.child1.guide) <= 0)) {
               sum += p.value;
               search(p.child1, x, h-1, sum);
           }
           else {
               sum += p.value;
               search(p.child2, x, h-1, sum);
           }
       }
   }
  
   static void printLE(Node init, String x, int h, int inc) throws Exception {
      if (h == 0) {
         if (x.compareTo(init.guide) >= 0) {
           init.value = init.value + inc;
           return;
           }
      }
      else {
          InternalNode p = (InternalNode) init;
          if (x.compareTo(p.child0.guide) <= 0) {
                printLE(p.child0, x, h-1, inc);
          }
          else if ((p.child2.equals(null)) || (x.compareTo(p.child1.guide) <= 0)) {
             p.child0.value += inc;
             printLE(p.child1, x, h-1, inc);
          }
          else {
            p.child0.value += inc;
            p.child1.value += inc;
            printLE(p.child2, x, h-1, inc);
          }
      }
   }
 
   static void printGE(Node init, String x, int h, int inc) throws Exception {
      if (h == 0) {
         if (x.compareTo(init.guide) <= 0) {
           init.value = init.value + inc;
             return;
         }
      }
      else {
          InternalNode p = (InternalNode) init;
          if (x.compareTo(p.child0.guide) >= 0) {
             printGE(p.child0, x, h-1,inc);
          }
          else if ((p.child2.equals(null)) || (x.compareTo(p.child1.guide)) >= 0) {
             p.child0.value += inc;
             printGE(p.child1, x, h-1,inc);
          }
          else {
             p.child0.value += inc;
             p.child1.value += inc;
             printGE(p.child2, x, h-1, inc);
          }
      }
   }
 
   static void addRange(Node init, String x, String y, int h, int inc) throws Exception {
      InternalNode p = (InternalNode) init;
      if (y.compareTo(p.child0.guide) <= 0) {
         addRange(p.child0, x, y, h-1, inc);
      }
      else if ((p.child2 == null) || (y.compareTo(p.child1.guide) <= 0)) {
         if (x.compareTo(p.child0.guide) <= 0) {
            printGE(p.child0,x,h-1, inc);
            printLE(p.child1,y,h-1, inc);
         }
         else {
            addRange(p.child1, x, y, h-1, inc);
         }
      }
      else {
         if (x.compareTo(p.child0.guide) <= 0) {
            printGE(p.child0, x, h-1, inc);
            p.child1.value += inc;
            printLE(p.child2, y, h-1, inc);
         }
         else if ((x.compareTo(p.child1.guide) <= 0)) {
            printGE(p.child1, x, h-1, inc);
            printLE(p.child2, y, h-1, inc);
         }
         else {
            addRange(p.child2, x, y, h-1, inc);
         }
      }
   }
}

class Node {
   String guide;
   int value;
}

class InternalNode extends Node {
   Node child0, child1, child2;
}

class LeafNode extends Node {

}

class TwoThreeTree {
   Node root;
   int height;

  TwoThreeTree() {
     root = null;
     height = -1;
  }
}

class WorkSpace {
   // this class is used to hold return values for the recursive doInsert
   // routine (see below)
   
   Node newNode;
   int offset;
   boolean guideChanged;
   Node[] scratch;
   
}
