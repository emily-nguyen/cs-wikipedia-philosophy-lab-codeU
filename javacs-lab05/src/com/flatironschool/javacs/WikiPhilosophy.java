package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;

import java.util.Deque; 
import java.util.ArrayDeque; 

public class WikiPhilosophy {
	
	final static WikiFetcher wf = new WikiFetcher();
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		// Stores the list of visited urls 
		List<String> visited = new ArrayList<String>();

		// From Java page, it should take 7 links to get to Philosphy page 
		int limit = 7; 
		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		String endUrl = "https://en.wikipedia.org/wiki/Philosophy";

		// TODO: Avoid hardcoding number of iterations
		for (int i=0; i < limit; i++) {
			if (visited.contains(url)) {
				System.err.println("Invalid url: This link has already been visited.");
				return; 
			} 

			visited.add(url);

			// Fetch webpage main content 
			Elements paragraphs = wf.fetchWikipedia(url);

			// Parse html to find first valid url link 
			Element element = getFirstLink(paragraphs);	
		
			if (element == null) {
				System.err.println("Invalid url: This page has no valid links.");
				return;
			}

			// Convert url to absolute version 
			url = element.absUrl("href");

			if (url.equals(endUrl)) {
				visited.add(url);

				System.out.println("Found the Philosophy page.");

				// Print out all the urls 
				for (int j=0; j < visited.size(); j++) {
					System.out.println(visited.get(j));
				}

				return; 
			}
		}
	}

	private static Element getFirstLink(Elements paragraphs) {
		for (Element para: paragraphs) {
			Element firstLink = getFirstLinkInPara(para);

			// Return the first valid link on the page 
			if (firstLink != null) {
				return firstLink;
			}
		}

		return null;
	}

	private static Element getFirstLinkInPara(Node root) {
		Iterable<Node> iter = new WikiNodeIterable(root); 

		for (Node node: iter) {
			if (node instanceof Element) {				
				// Get list of all the links for this element 
				Elements links = ((Element) node).select("a");

				// If there is at least one link, check if it's valid 
				if (links.size() > 0) {
					int index = 0; 

					// Keep track of links w/in parentheses 
					Deque<String> parenStack = new ArrayDeque<String>();

					// Split element by spaces 
					String[] words = node.toString().split(" ");

				    // Iterate through element text to find first valid link 
					for (int i=0; i < words.length; i++) {
						if (words[i].startsWith("(")) {
							parenStack.push(words[i]);
						} else if (words[i].endsWith(")") && !parenStack.isEmpty()) {
							parenStack.pop();
						} else if (words[i].startsWith("<a")) {		// If it's a link 
							if (!parenStack.isEmpty()) {
								// If link is w/in parentheses, skip to next link in list
								index += 1;
							}

							break;								
						}
					}	

					Element firstLink = links.get(index);
					return firstLink;		
				}				
			}	
		}

		return null;
	}

	private static boolean isItalic(Element root) {
		// Check to see if tag in the parent chain is in italics = invalid link 
		for (Element element=root; element != null; element.parent()) {
			if (element.tagName().equals("i") || element.tagName().equals("em")) {
				return true;
			}
		}

		return false; 
	}
}