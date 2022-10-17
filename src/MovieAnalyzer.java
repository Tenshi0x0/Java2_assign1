import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

class PSS {
    List<String> w;

    public PSS() {
        w = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof PSS t) {
            return w.get(0).equals(t.w.get(0)) && w.get(1).equals(t.w.get(1));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(w.get(0), w.get(1));
    }
}

class PIS implements Comparable {
    int x;
    String y;

    public PIS(int x, String y) {
        this.x = x;
        this.y = y;
    }


    @Override
    public int compareTo(Object o) {
        PIS t = (PIS) o;
        if (x != t.x) return x - t.x;
        return -y.compareTo(t.y);
    }
}

class PSSC implements Comparable {
    String x;
    String y;

    public PSSC(String x, String y) {
        this.x = x;
        this.y = y;
    }


    @Override
    public int compareTo(Object o) {
        PSSC t = (PSSC) o;
        if (t.x.length() != x.length()) return x.length() - t.x.length();
        return t.y.compareTo(y);
    }
}

class NodeAVG implements Comparable {
    double sum;
    int cnt;
    String str;

    public NodeAVG(double sum, int cnt, String str) {
        this.sum = sum;
        this.cnt = cnt;
        this.str = str;
    }

    @Override
    public int compareTo(Object o) {
        NodeAVG t = (NodeAVG) o;
        String fir = String.valueOf(sum / cnt);
        String sec = String.valueOf(t.sum / t.cnt);
        if (fir.equals(sec)) return str.compareTo(t.str);

        if (sum / cnt > t.sum / t.cnt) return -1;
        return 1;
    }
}

public class MovieAnalyzer {
    List<String[]> w = new ArrayList<>();

    public MovieAnalyzer(String path) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
        bufferedReader.readLine();
        String str;
        while ((str = bufferedReader.readLine()) != null) {
            String[] rec = str.trim().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            w.add(rec);
        }
    }

    public Map<Integer, Integer> getMovieCountByYear() {
        Map<Integer, Integer> res = new LinkedHashMap<>();
        int[] b = new int[3030];
        for (String[] e : w) {
            int val = Integer.parseInt(e[2]);
            b[val]++;
        }
        for (int i = 3000; i >= 1900; i--) if (b[i] > 0) res.put(i, b[i]);
        return res;
    }

    public Map<String, Integer> getMovieCountByGenre() {
        Map<String, Integer> b = new LinkedHashMap<>();
        for (String[] e : w)
            if (e[5] != null) {
                String str = e[5];
                str = str.replaceAll("[ \"]", "");
                String[] strs = str.split(",");
                for (String t : strs) {
                    if (b.containsKey(t)) b.put(t, b.get(t) + 1);
                    else b.put(t, 1);
                }
            }
        List<Map.Entry<String, Integer>> buf = new ArrayList<>(b.entrySet());
        buf.sort(new Comparator<>() {
            @Override
            public int compare(Map.Entry<String, Integer> fir, Map.Entry<String, Integer> sec) {
                if (fir.getValue() != sec.getValue()) {
                    return sec.getValue() - fir.getValue();
                } else {
                    return fir.getKey().compareTo(sec.getKey());
                }
            }
        });

        Map<String, Integer> res = new LinkedHashMap<>();
        buf.forEach(e -> res.put(e.getKey(), e.getValue()));
        return res;
    }

    public Map<List<String>, Integer> getCoStarCount() {
        Map<PSS, Integer> b = new HashMap<>();
        for (String[] e : w) {
            for (int i = 10; i <= 13; i++)
                for (int j = i + 1; j <= 13; j++) {
                    String fir = e[i], sec = e[j];
                    if (fir == null || sec == null) continue;
                    PSS pss = new PSS();
                    if (fir.compareTo(sec) > 0) {
                        String t = sec;
                        sec = fir;
                        fir = t;
                    }
                    pss.w.add(fir);
                    pss.w.add(sec);
                    if (b.containsKey(pss)) b.put(pss, b.get(pss) + 1);
                    else b.put(pss, 1);
                }
        }
        Map<List<String>, Integer> res = new HashMap<>();
        b.forEach((fir, sec) -> {
            res.put(fir.w, sec);
        });
        return res;
    }

    public List<String> getTopMovies(int top_k, String by) {
        List<String> res = new ArrayList<>();
        if (by.equals("runtime")) {
            PriorityQueue<PIS> q = new PriorityQueue<>();
            for (String[] e : w) {
                int val = Integer.parseInt(e[4].replace(" min", ""));
                q.add(new PIS(val, e[1].replace("\"", "")));
                if (q.size() > top_k) q.remove();
            }
            while (top_k > 0 && q.size() > 0) {
                res.add(q.remove().y);
                top_k--;
            }
            Collections.reverse(res);
            return res;
        } else if (by.equals("overview")) {
            PriorityQueue<PSSC> q = new PriorityQueue<>();
            for (String[] e : w) {
                String tmp = e[7];
//                q.add(new PSSC(tmp, e[1].replace("\"", "")));
                if (tmp.charAt(0) == '"') q.add(new PSSC(tmp.substring(1, tmp.length() - 1), e[1].replace("\"", "")));
                else q.add(new PSSC(tmp, e[1].replace("\"", "")));
                if (q.size() > top_k) q.remove();
            }
            while (top_k > 0 && q.size() > 0) {
                res.add(q.remove().y);
                top_k--;
            }
            Collections.reverse(res);
            return res;
        } else return res;
    }

    public List<String> getTopStars(int top_k, String by) {
        List<String> res = new ArrayList<>();
        if (by.equals("rating")) {
            Map<String, NodeAVG> b = new HashMap<>();
            for (String[] e : w) {
                for (int i = 10; i <= 13; i++) {
                    String str = e[i];
                    float sum = Float.parseFloat(e[6]);
                    NodeAVG rec;
                    if (b.containsKey(str)) {
                        rec = b.get(str);
                        rec.sum += sum;
                        rec.cnt++;
                    } else rec = new NodeAVG(sum, 1, str);
                    b.put(str, rec);
                }
            }

            PriorityQueue<NodeAVG> q = new PriorityQueue<>();
            b.forEach((fir, sec) -> q.add(sec));
            while (top_k > 0 && q.size() > 0) {
                res.add(q.remove().str);
                top_k--;
            }
            return res;
        } else {
            Map<String, NodeAVG> b = new HashMap<>();
            for (String[] e : w) {
                for (int i = 10; i <= 13; i++) {
                    String str = e[i];
                    if (e[15] == null || e[15].equals("")) continue;
                    double sum = Double.parseDouble(e[15].replace(",", "").replace("\"", ""));
                    NodeAVG rec;
                    if (b.containsKey(str)) {
                        rec = b.get(str);
                        rec.sum += sum;
                        rec.cnt++;
                    } else rec = new NodeAVG(sum, 1, str);
                    b.put(str, rec);
                }
            }

            PriorityQueue<NodeAVG> q = new PriorityQueue<>();
            b.forEach((fir, sec) -> q.add(sec));
            while (top_k > 0 && q.size() > 0) {
                res.add(q.remove().str);
                top_k--;
            }
            return res;
        }
    }

    public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
        List<String> rec = new ArrayList<>();
        for (String[] e : w) {
            String[] buf = e[5].replace("\"", "").replace(" ", "").split(",");
            for (String t : buf) {
                if (!t.equals(genre)) continue;
                if (Float.parseFloat(e[6]) < min_rating) continue;
                if (Integer.parseInt(e[4].replace(" min", "")) > max_runtime) continue;
                String tt = e[1];
                if (tt.charAt(0) == '"') rec.add(tt.substring(1, tt.length() - 1));
                else rec.add(tt);
            }
        }

        String[] b = new String[rec.size()];
        int tot = 0;
        for (String e : rec) b[tot++] = e;
        Arrays.sort(b);
        return List.of(b);
    }
}